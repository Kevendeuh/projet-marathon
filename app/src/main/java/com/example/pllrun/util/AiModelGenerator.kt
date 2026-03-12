package com.example.pllrun.util

import android.content.Context
import android.util.Log
import ai.mlc.mlcllm.MLCEngine
import ai.mlc.mlcllm.OpenAIProtocol
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.InventaireViewModel
import com.example.pllrun.calculator.ApportsNutritionnels
import com.example.pllrun.calculator.CaloriesGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.Period

/**
 * Generates personalised nutrition recipe suggestions using a local MLC LLM (Mistral)
 * running on-device with the Vulkan backend (required on Pixel 8 Pro).
 *
 * Lifecycle:
 *  - The [MLCEngine] is created **lazily** on first use to avoid spawning background
 *    threads before they are needed.
 *  - Call [unload] when the owning ViewModel is cleared.
 */
class NutritionAiGenerator(private val context: Context) {

    companion object {
        private const val TAG = "NutritionAI"

        /**
         * The name passed to [MLCEngine.reload] as `modelLib`.
         * Must match the stem of the compiled model cartridge .so:
         *   → jniLibs/arm64-v8a/lib<MODEL_LIB>.so
         */
        private const val MODEL_LIB = "mistral_q4f16_1"

        /**
         * Name of the directory (inside the app's external-files dir OR on external storage)
         * that contains mlc-chat-config.json + tokenizer files + *.ndarray weights.
         */
        private const val MODEL_DIR_NAME = "mistral-nutritionist-v1"

        private const val MAX_NEW_TOKENS = 900
        private const val TEMPERATURE = 0.75f
    }

    // ---------------------------------------------------------------------------
    // Engine — created lazily so no background threads start until first inference
    // ---------------------------------------------------------------------------

    /**
     * We use a nullable private backing field so we can null it out after unload(),
     * preventing any further use of a destroyed native object.
     */
    private var _engine: MLCEngine? = null
    private var isModelLoaded = false

    private fun getOrCreateEngine(): MLCEngine {
        return _engine ?: MLCEngine().also { _engine = it }
    }

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    /**
     * Generates a personalised nutrition / recipe suggestion for [utilisateur].
     * Must be called from a coroutine (it suspends and runs on [Dispatchers.IO]).
     *
     * @return A formatted recipe string, or a localised error message on failure.
     */
    suspend fun genererSuggestionRepas(utilisateur: Utilisateur, viewModel: InventaireViewModel): Flow<String> = flow {
        try {
            ensureModelLoaded()
            val engine = _engine
            if (engine == null) {
                emit("Erreur : moteur non initialisé.")
                return@flow
            }

            val systemPrompt = buildSystemPrompt(
                utilisateur = utilisateur,
                apports = viewModel.getRecommendedNutriments(utilisateur.id,).value ?: ApportsNutritionnels(0F,0F,0F,0F)
            )
            val userPrompt  = buildUserPrompt(utilisateur)

            Log.d(TAG, "Envoi du prompt…")

            val messages = listOf(
                OpenAIProtocol.ChatCompletionMessage(
                    role    = OpenAIProtocol.ChatCompletionRole.system,
                    content = systemPrompt
                ),
                OpenAIProtocol.ChatCompletionMessage(
                    role    = OpenAIProtocol.ChatCompletionRole.user,
                    content = userPrompt
                )
            )

            val channel = engine.chat.completions.create(
                messages    = messages,
                temperature = TEMPERATURE,
                max_tokens  = MAX_NEW_TOKENS,
                stream      = true
            )

            val sb = StringBuilder()
            for (response in channel) {
                val token = response.choices.firstOrNull()?.delta?.content?.asText()
                if (token != null) {
                    sb.append(token)
                    emit(sb.toString())
                }
            }

            val result = sb.toString().trim()
            if (result.isBlank()) {
                emit("Le modèle n'a produit aucune réponse. Essayez de nouveau.")
            } else {
                Log.i(TAG, "Génération terminée (${result.length} caractères).")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erreur pendant l'inférence", e)
            // Reset so the next call will try to reload the model
            isModelLoaded = false
            emit("Erreur : ${e.message ?: "inconnue"}")
        }
    }.flowOn(Dispatchers.IO)

    /** Releases native resources. Call from ViewModel.onCleared(). */
    fun unload() {
        try {
            _engine?.unload()
            Log.i(TAG, "Moteur déchargé.")
        } catch (e: Exception) {
            Log.w(TAG, "Erreur lors du déchargement", e)
        } finally {
            _engine = null
            isModelLoaded = false
        }
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Locates the model directory and calls [MLCEngine.reload] if needed.
     *
     * Priority:
     *  1. App-private external dir   → getExternalFilesDir(null)/<MODEL_DIR_NAME>
     *  2. Public Downloads folder    → Environment.DIRECTORY_DOWNLOADS/<MODEL_DIR_NAME>
     *
     * The second path is convenient for ADB-pushing the model without root.
     */
    private fun ensureModelLoaded() {
        if (isModelLoaded) return

        val candidates = listOf(
            File(context.getExternalFilesDir(null), MODEL_DIR_NAME),
            File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ),
                MODEL_DIR_NAME
            )
        )

        val modelDir = candidates.firstOrNull { it.exists() && File(it, "mlc-chat-config.json").exists() }
            ?: throw IllegalStateException(
                "Modèle introuvable. Copiez le dossier '$MODEL_DIR_NAME' dans l'un des emplacements suivants :\n" +
                candidates.joinToString("\n") { "  • ${it.absolutePath}" }
            )

        Log.i(TAG, "Chargement du modèle depuis : ${modelDir.absolutePath}")

        // Step 1: Create engine (loads libtvm4j_runtime_packed + libmlc_llm_jni).
        val engine = getOrCreateEngine()

        // Step 2: Load the compiled model cartridge (.so).
        // libmistral_nutritionist.so was patched with patchelf to add
        // libtvm4j_runtime_packed.so as a DT_NEEDED entry, so System.loadLibrary()
        // can now resolve TVMFFIFunctionCall at dlopen time.
        // Its static initialisers run and register model functions (vm_load_executable, …)
        // as TVM system lib globals — which the engine then finds via "system://" lookup.
        System.loadLibrary("mistral_nutritionist")
        Log.i(TAG, "Bibliothèque modèle chargée : libmistral_nutritionist.so")

        // Step 3: Ask the background loop to load weights + initialise the engine.
        engine.reload(modelDir.absolutePath, MODEL_LIB)
        isModelLoaded = true

        Log.i(TAG, "✅ Modèle chargé avec succès (Vulkan).")
    }

    // ---------------------------------------------------------------------------
    // Prompt construction
    // ---------------------------------------------------------------------------

    /**
     * System prompt: establishes the expert persona and output format.
     * Keeping it concise reduces prefill time on device.
     */
 

    fun buildSystemPrompt(
        utilisateur: Utilisateur,
        apports: ApportsNutritionnels
    ): String {
        // 1. Calcul de l'âge identique à ton CaloriesGenerator
        val age = utilisateur.dateDeNaissance?.let {
            Period.between(it, LocalDate.now()).years.toString()
        } ?: "unknown"

        // 2. Traduction du sexe pour l'IA (en anglais)
        val genderEn = when (utilisateur.sexe) {
            Sexe.HOMME -> "male"
            Sexe.FEMME -> "female"
            else -> "person"
        }

        // 3. Construction du prompt
        // On utilise .toInt() sur tes Float pour donner des chiffres ronds au LLM
        return """
        You are an expert sports nutritionist specializing in athletic performance.
        
        Your task is to create ONE single, realistic meal recipe tailored for a $age-year-old $genderEn, height: ${utilisateur.taille}cm, weight: ${utilisateur.poids}kg.
        
        The user's DAILY nutritional targets are: 
        - Calories: ${apports.calories.toInt()} kcal
        - Protein: ${apports.proteines.toInt()}g
        - Carbohydrates: ${apports.glucides.toInt()}g
        - Fats: ${apports.lipides.toInt()}g
        
        The meal you provide should represent an appropriate portion of these daily targets (e.g., roughly 30-40% of the daily intake).

        CRITICAL INSTRUCTIONS:
        1. Provide realistic recipes using a maximum of 15 ingredients.
        2. Each ingredient must be explicitly listed in the INGREDIENTS section exactly once.
        3. Be concise and highly structured.
        4. You MUST output the final response in ENGLISH, strictly following the exact template below. Do NOT add any conversational introductory or concluding text.

        TEMPLATE (Translate the dynamic values, but keep these exact headers):
        NOM DE LA RECETTE
        Temps de préparation : X min | Temps de cuisson : X min

        INGRÉDIENTS (pour 1 portion)
        - [quantité] [ingrédient]

        PRÉPARATION (étapes numérotées, courtes)
        1. ...

        VALEURS NUTRITIONNELLES (estimées pour ce repas)
        Calories : X kcal | Protéines : Xg | Glucides : Xg | Lipides : Xg
    """.trimIndent()
    }

    /**
     * User prompt: injects all relevant [Utilisateur] metrics.
     * More context = more personalised output from a small 7B model.
     */
    private fun buildUserPrompt(u: Utilisateur): String {
        val age    = u.dateDeNaissance
            ?.let { Period.between(it, LocalDate.now()).years }
            ?.let { "$it ans" } ?: "âge non renseigné"

        val sexe   = when (u.sexe) {
            Sexe.HOMME       -> "Homme"
            Sexe.FEMME       -> "Femme"
            Sexe.AUTRE       -> "Autre"
            Sexe.NON_SPECIFIE -> "Non spécifié"
        }

        val niveau = when (u.niveauExperience) {
            NiveauExperience.DEBUTANT      -> "Débutant (< 6 mois de course régulière)"
            NiveauExperience.INTERMEDIAIRE -> "Intermédiaire (6 mois–2 ans, semi-marathons)"
            NiveauExperience.AVANCE        -> "Avancé (> 2 ans, plusieurs marathons)"
        }

        val joursCourse = u.joursEntrainementDisponibles
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
            ?: "Non spécifiés"

        // Harris-Benedict BMR → rough total daily energy estimate
        val bmr: Double = when (u.sexe) {
            Sexe.FEMME -> {
                val ageYears = u.dateDeNaissance?.let { Period.between(it, LocalDate.now()).years }?.toDouble() ?: 30.0
                (10.0 * u.poids) + (6.25 * u.taille) - (5.0 * ageYears) - 161.0
            }
            else -> {
                val ageYears = u.dateDeNaissance?.let { Period.between(it, LocalDate.now()).years }?.toDouble() ?: 30.0
                (10.0 * u.poids) + (6.25 * u.taille) - (5.0 * ageYears) + 5.0
            }
        }
        // Activity multiplier based on experience (facteur field)
        val tdee = (bmr * u.niveauExperience.facteur).toInt()

        val vmaInfo = u.vma?.takeIf { it > 0.0 }?.let { "VMA = ${"%.1f".format(it)} km/h" } ?: "VMA non renseignée"
        val fcmInfo = u.fcm?.takeIf { it > 0 }?.let { "FCmax = $it bpm" } ?: "FCmax non renseignée"
        val fcrInfo = u.fcr?.takeIf { it > 0 }?.let { "FCR = $it bpm" } ?: "FCR non renseignée"

        return """
            Génère une recette post-entraînement adaptée au profil ci-dessous.
            La recette doit favoriser la récupération musculaire et la recharge glycogénique
            pour un sportif.

            === PROFIL ATHLÈTE ===
            Prénom     : ${u.prenom.ifBlank { "Non renseigné" }}
            Âge        : $age
            Sexe       : $sexe
            Poids      : ${u.poids} kg  (objectif : ${u.poidsCible} kg)
            Taille     : ${u.taille} cm
            Niveau     : $niveau
            Jours course: $joursCourse

            === DONNÉES PHYSIOLOGIQUES ===
            $vmaInfo | $fcmInfo | $fcrInfo
            Besoins caloriques estimés (TDEE) : ~$tdee kcal/jour

            === CONTRAINTES ===
            - La recette doit couvrir environ 25–30 % des besoins journaliers (~${tdee / 4} kcal)
            - Ratio idéal post-effort : 3:1 glucides/protéines
            - Ingrédients simples, rapides à préparer (< 20 min au total)
            - Favoriser les aliments anti-inflammatoires si possible
        """.trimIndent()
    }
}