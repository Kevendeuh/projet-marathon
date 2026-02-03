package com.example.pllrun.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ai.mlc.mlcllm.MLCEngine
import ai.mlc.mlcllm.OpenAIProtocol
import com.example.pllrun.Classes.Utilisateur
import java.time.LocalDate
import java.time.Period
import java.util.UUID

class NutritionAiGenerator {

    private val engine = MLCEngine()
    private var isModelLoaded = false

    private val modelPath = "mlc-model/mistral-7b-nutritionist-q4f16_1"
    private val modelLib = "mistral_q4f16_1"

    suspend fun genererSuggestionRepas(utilisateur: Utilisateur): String {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Chargement
                if (!isModelLoaded) {
                    engine.reload(modelPath, modelLib)
                    isModelLoaded = true
                }

                val prompt = construirePromptNutrition(utilisateur)

                // 2. Messages
                val messages = listOf(
                    OpenAIProtocol.ChatCompletionMessage(
                        role = OpenAIProtocol.ChatCompletionRole.system,
                        content = "Tu es un nutritionniste. Réponds avec une recette."
                    ),
                    OpenAIProtocol.ChatCompletionMessage(
                        role = OpenAIProtocol.ChatCompletionRole.user,
                        content = prompt
                    )
                )

                // 3. Appel à l'IA (Signature corrigée selon ton erreur)
                // Cela retourne un ReceiveChannel, pas un Flow
                val channel = engine.chat.completions.create(
                    messages = messages,
                    temperature = 0.7f,
                    max_tokens = 600,
                    stream = true
                    // J'ai retiré request_id car ton erreur montrait qu'il n'était pas dans la liste des arguments valides
                )

                val stringBuilder = StringBuilder()

                // 4. Lecture du Canal (La correction est ICI)
                // On utilise une boucle for standard pour consommer un Channel
                for (response in channel) {
                    // Ici, 'response' est typé correctement en ChatCompletionStreamResponse
                    if (response.choices.isNotEmpty()) {
                        val part = response.choices[0].delta.content
                        if (part?.parts?.isNotEmpty() == false) {
                            stringBuilder.append(part)
                        }
                    }
                }

                val reponseFinale = stringBuilder.toString()

                if (reponseFinale.isBlank()) return@withContext "Erreur : Réponse vide."

                formaterReponse(reponseFinale)

            } catch (e: Exception) {
                e.printStackTrace()
                isModelLoaded = false
                "Erreur : ${e.message}"
            }
        }
    }

    private fun construirePromptNutrition(utilisateur: Utilisateur): String {
        val age = if (utilisateur.dateDeNaissance != null) {
            Period.between(utilisateur.dateDeNaissance, LocalDate.now()).years.toString() + " ans"
        } else {
            "Non spécifié"
        }
        val sexe = utilisateur.sexe?.toString() ?: "Autre"
        val niveau = utilisateur.niveauExperience?.toString() ?: "Débutant"

        return """
        [INST]
        Athlète : $age, $sexe, ${utilisateur.poids}kg, ${utilisateur.taille}cm. Niveau: $niveau.
        Objectif : Marathon.
        Recette de récupération (Plat, Calories, Ingrédients, Pourquoi).
        [/INST]
        """.trimIndent()
    }

    private fun formaterReponse(reponse: String): String {
        return reponse.trim()
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