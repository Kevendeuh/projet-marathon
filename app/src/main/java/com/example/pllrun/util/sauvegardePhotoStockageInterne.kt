package com.example.pllrun.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // Crée un nom de fichier unique pour éviter les conflits
        val fileName = "profile_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        // Retourne l'URI du NOUVEAU fichier que vous contrôlez
        return Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}