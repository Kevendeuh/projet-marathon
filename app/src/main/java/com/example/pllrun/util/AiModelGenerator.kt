package com.example.pllrun.util

import android.content.Context
import android.util.Log
import ai.mlc.mlcllm.MLCEngine
import ai.mlc.mlcllm.OpenAIProtocol
import com.example.pllrun.Classes.Utilisateur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.Period

class NutritionAiGenerator(private val context: Context) {

    private val engine: MLCEngine;
    private var isModelLoaded = false;

    // --- CONFIGURATION MISTRAL ---
    // Nom physique de la lib .so chargée (ne change pas ça)
    //private val modelLib = "mistral_q4f16_1_7ee501c699c01f7e2965790fad74bc79"
    private val modelLib = "mistral_nutritionist"
    //private val assetModelDir = "mlc-model"
    private val assetModelDir = "dist/mistral-nutritionist"
    // Nom du dossier sur le téléphone (Destination)
    // On met un nom clair pour s'y retrouver
    //private val deviceModelDir = "dist/mistral-7b-nutritionist-q4f16_1-MLC"
    private val deviceModelDirName = "mistral-nutritionist-v1"
    private val externalModelDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "mistral-nutritionist-v1")
    init {
        val appDocDir = context.getExternalFilesDir(null)
        val finalModelDir = File(appDocDir, deviceModelDirName)
        // Plus de copie d'assets !
        Log.i("MLC_INIT", "Recherche du modèle dans : ${externalModelDir.absolutePath}")

        val configFile = File(externalModelDir, "mlc-chat-config.json")

        if (externalModelDir.exists() && configFile.exists()) {
            Log.i("MLC_INIT", "✅ Modèle trouvé sur le stockage externe.")
        } else {
            Log.e("MLC_INIT", "❌ MODÈLE MANQUANT ! Utilise ADB pour copier les fichiers.")
            // Tu pourras afficher une erreur à l'utilisateur ici
        }

        engine = MLCEngine()
    }

    suspend fun genererSuggestionRepas(utilisateur: Utilisateur): String {
        return withContext(Dispatchers.IO) {
            try {
                // 2. Chargement du modèle Mistral
                if (!isModelLoaded) {
                    val appDocDir = context.getExternalFilesDir(null)
                    val modelPath = File(appDocDir, deviceModelDirName).absolutePath

                    Log.i("MLC_RUN", "Chargement depuis : $modelPath")
                    engine.reload(modelPath, modelLib)
                    isModelLoaded = true
                }

                val promptUtilisateur = construirePromptNutrition(utilisateur)

                // 3. Messages au format OpenAI (MLC convertira en [INST] grâce au JSON)
                val messages = listOf(
                    OpenAIProtocol.ChatCompletionMessage(
                        role = OpenAIProtocol.ChatCompletionRole.system,
                        content = "Tu es un nutritionniste expert sportif. Tu dois répondre en français avec une recette précise."
                    ),
                    OpenAIProtocol.ChatCompletionMessage(
                        role = OpenAIProtocol.ChatCompletionRole.user,
                        content = promptUtilisateur
                    )
                )

                // 4. Lancement de la génération
                val channel = engine.chat.completions.create(
                    messages = messages,
                    temperature = 0.7f,
                    max_tokens = 800, // Mistral est bavard, on laisse de la place
                    stream = true
                )

                val stringBuilder = StringBuilder()

                for (response in channel) {
                    if (response.choices.isNotEmpty()) {
                        val content = response.choices[0].delta.content
                        if (content != null) {
                            stringBuilder.append(content)
                        }
                    }
                }

                val reponseFinale = stringBuilder.toString()
                if (reponseFinale.isBlank()) return@withContext "Erreur : Mistral n'a rien répondu."

                formaterReponse(reponseFinale)

            } catch (e: Exception) {
                Log.e("MLC_ERROR", "Erreur Mistral", e)
                isModelLoaded = false
                "Erreur technique : ${e.message}"
            }
        }
    }

    private fun construirePromptNutrition(utilisateur: Utilisateur): String {
        val age = if (utilisateur.dateDeNaissance != null) {
            Period.between(utilisateur.dateDeNaissance, LocalDate.now()).years.toString() + " ans"
        } else {
            "Non spécifié"
        }
        val sexe = utilisateur.sexe?.toString() ?: "Non spécifié"
        val niveau = utilisateur.niveauExperience?.toString() ?: "Débutant"

        return """
        Crée une recette de récupération pour la musculation.
        
        Profil :
        - Age: $age ans
        - Sex: $sexe
        - Poids: ${utilisateur.poids}kg (Objectif de poids: ${utilisateur.poidsCible}kg)
        - Taille: ${utilisateur.taille}cm
        - Niveau: $niveau
        
        Format attendu :
        TITRE
        INGREDIENTS (avec quantités)
        INSTRUCTIONS (courtes)
        MACROS (Protéines/Glucides/Lipides)
        """.trimIndent()
    }

    private fun formaterReponse(reponse: String): String {
        return reponse.trim()
    }

    private fun copyAssets(context: Context, assetPath: String, destDir: File) {
        if (!destDir.exists()) destDir.mkdirs()

        val assets = context.assets.list(assetPath) ?: return

        if (assets.isEmpty()) {
            Log.e("MLC_INIT", "ERREUR: Le dossier Assets/$assetPath est vide sur le PC !")
        }

        for (fileName in assets) {
            try {
                // On évite les fichiers systèmes ou cachés
                if (fileName.startsWith(".")) continue

                // --- CORRECTION DU "DOSSIER DANS DOSSIER" ---
                // Si par erreur il y a un sous-dossier, on l'ignore pour ne copier que les fichiers
                // (Sauf si tu as rangé tes poids dans un sous-dossier params, mais restons simple)
                val inputStream = context.assets.open("$assetPath/$fileName")
                val outFile = File(destDir, fileName)

                // Petite optimisation : ne pas écraser si déjà présent et > 0 octets
                if (outFile.exists() && outFile.length() > 0) continue

                Log.i("MLC_INIT", "Copie : $fileName")
                val outputStream = FileOutputStream(outFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            } catch (e: Exception) {
                // Ce catch gère le cas où "fileName" est un dossier (open lancera une exception)
                Log.w("MLC_INIT", "Ignoré (probablement un dossier) : $fileName")
            }
        }
    }

    fun unload() {
        try {
            engine.unload()
            isModelLoaded = false
        } catch (e: Exception) {
            Log.e("IA", "Erreur unload", e)
        }
    }
}