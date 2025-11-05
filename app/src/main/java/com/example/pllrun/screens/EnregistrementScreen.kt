package com.example.pllrun.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.NiveauExperience
import com.example.pllrun.Classes.Utilisateur
import com.example.pllrun.Classes.JourSemaine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.pllrun.Classes.TypeDecoupage
import com.example.pllrun.Classes.Sexe
import com.example.pllrun.InventaireViewModel

import com.example.pllrun.R
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pllrun.components.DatePickerComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.time.LocalDate
import kotlin.text.lowercase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnregistrementScreen(
    viewModel: InventaireViewModel?,
    utilisateurId: Long?,
    onNext: () -> Unit,
) {
    // --- 1. DÉTECTION DU MODE ---
    val isEditMode = utilisateurId != null

    // --- ÉTATS POUR L'IMAGE ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var imageVersion by remember { mutableStateOf(0L) }


    // --- LANCEURS POUR LES ACTIVITÉS ---
    // Lanceur pour sélectionner une image depuis la galerie
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            imageVersion = System.currentTimeMillis()
        }
    }

    // Lanceur pour prendre une photo avec l'appareil photo
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La photo a été prise et sauvegardée, on met à jour notre clé de version.
            imageVersion = System.currentTimeMillis()
        } else {
            // L'utilisateur a annulé, on efface l'URI pour éviter une image vide.
            imageUri = null
        }
    }

    // --- GESTION DES PERMISSIONS ---
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(
        // On vérifie l'état initial de la permission
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    )}

    // Launcher pour demander la permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // La permission vient d'être accordée, on lance l'appareil photo MAINTENANT.
            val uri = createImageUriForCamera(context)
            if (uri != null) {
                imageUri = uri
                takePictureLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Impossible de créer le fichier image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // L'utilisateur a refusé la permission.
            Toast.makeText(context, "La permission de la caméra est requise pour prendre une photo.", Toast.LENGTH_LONG).show()
        }
    }

    // États pour les champs de formulaire
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var poids by remember { mutableStateOf("") }
    var taille by remember { mutableStateOf("") }
    var niveau by remember { mutableStateOf(NiveauExperience.DEBUTANT) }
    var dateDeNaissance by remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    var sexe by remember { mutableStateOf(Sexe.NON_SPECIFIE) }

    // États pour les champs optionnels
    var poidsCible by remember { mutableStateOf("") }
    var vma by remember { mutableStateOf("") }
    var fcm by remember { mutableStateOf("") }
    var fcr by remember { mutableStateOf("") }

    //variable pour le bouton de suppression utilisateur
    //var utilisateurPrincipal by remember { mutableStateOf<Utilisateur>(Utilisateur()) }

    // États pour les jours d'entraînement
    var joursSelectionnes by remember { mutableStateOf(setOf<JourSemaine>()) }

    // États pour les menus déroulants
    var sexeExpanded by remember { mutableStateOf(false) }
    var niveauExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Options pour les menus déroulants
    val isButtonEnabled = remember(nom, prenom, dateDeNaissance, joursSelectionnes, poidsCible) {
        isFormValid(nom=nom, prenom=prenom, dateDeNaissance= dateDeNaissance , poids = poids.toDoubleOrNull(), taille=taille.toIntOrNull(), joursEntrainementDisponibles = joursSelectionnes.toList() )
    }

    // --- 3. CHARGEMENT DES DONNÉES EN MODE MODIFICATION ---
    if (isEditMode && viewModel != null) {
        // On observe l'utilisateur à modifier depuis le ViewModel
        val userToEdit by viewModel.getUtilisateurById(utilisateurId).observeAsState()

        // LaunchedEffect se déclenchera une seule fois quand userToEdit sera chargé
        LaunchedEffect(userToEdit) {
            userToEdit?.let { user ->
                nom = user.nom
                poids = user.poids.toString()
                taille = user.taille.toString()
                dateDeNaissance = user.dateDeNaissance ?: LocalDate.now()
                niveau = user.niveauExperience
                joursSelectionnes = user.joursEntrainementDisponibles.toSet()
                vma = user.vma?.let { v -> if (v > 0.0) v.toString() else "" } ?: ""
                fcm = user.fcm?.let { v -> if (v > 0.0) v.toString() else "" } ?: ""
                fcr = user.fcr?.let { v -> if (v > 0.0) v.toString() else "" } ?: ""
                prenom = user.prenom
                if (user.imageUri != null) {
                    imageUri = user.imageUri!!.toUri()
                    // On force la mise à jour de l'image en changeant sa "version"
                    imageVersion = System.currentTimeMillis()
                } else {
                    imageUri = null
                }
                sexe = user.sexe
                poidsCible = user.poidsCible.toString()
            }
        }
    }


    // --- Fonction de Sauvegarde ---
    fun saveUser() {

        //met le poids cible au poids normal si non rempli
        if(poidsCible.isEmpty()){
            poidsCible = poids
        }

        viewModel?.addNewUtilisateur(
            nom = nom,
            prenom = prenom,
            imageUri = imageUri?.toString(),
            dateDeNaissance = dateDeNaissance,
            sexe = sexe,
            poids = poids.toDouble(),
            poidsCible = poidsCible.toDouble(),
            taille = taille.toInt(),
            vma = vma.toDoubleOrNull(),
            fcm = fcm.toIntOrNull(),
            fcr = fcr.toIntOrNull(),
            niveauExperience = niveau,
            joursEntrainementDisponibles = joursSelectionnes.toList() // On convertit le Set en List
        )
    }

    //fonction de suppression d'utilisateur
    /**
    LaunchedEffect(key1 = true) {
        val user = viewModel?.getAllUtilisateurs()?.firstOrNull()?.firstOrNull()
        if (user != null) {
            utilisateurPrincipal = user

        }
    }
    fun suppUser(){
        viewModel?.deleteUtilisateur(utilisateurPrincipal)
    }
    **/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFF1F1F1))
            .padding(24.dp), // Réduit le padding général
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Utilisation d'un Column scrollable pour gérer tous les champs
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // Ajout du scroll
            verticalArrangement = Arrangement.spacedBy(12.dp) // Réduit l'espacement
        ) {
            // Titre "Bienvenue !"
            Text(
                text = "Bienvenue !",
                fontSize = 22.sp, // Légèrement réduit
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp), // Réduit le padding
                textAlign = TextAlign.Center
            )

            // --- PHOTO DE PROFIL ---
            Box(
                modifier = Modifier
                    .size(120.dp) // Taille un peu plus grande pour la visibilité
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Affiche l'image sélectionnée ou l'icône par défaut
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri ?: R.drawable.user_icon)
                        .memoryCacheKey(imageVersion.toString()) // Clé de cache qui change
                        .diskCacheKey(imageVersion.toString()) // Clé de cache disque
                        .crossfade(true) // Effet de fondu agréable
                        .build(),
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                    contentScale = if (imageUri != null) ContentScale.Crop else ContentScale.Inside
                )

                // Icône appareil photo en bas à droite
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF751F)) // Couleur orange
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera), // Créez cette icône
                        contentDescription = "Changer de photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )

                    // --- MENU DÉROULANT POUR LE CHOIX DE LA SOURCE ---
                    DropdownMenu(
                        expanded = showImageSourceDialog,
                        onDismissRequest = { showImageSourceDialog = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ouvrir la galerie") },
                            onClick = {
                                showImageSourceDialog = false
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Prendre une photo") },
                            onClick = {
                                showImageSourceDialog = false
                                if (hasCameraPermission) {
                                    // La permission est déjà accordée, on lance l'appareil photo
                                    val uri = createImageUriForCamera(context)
                                    if (uri != null) {
                                        imageUri = uri
                                        takePictureLauncher.launch(uri)
                                    } else {
                                        Toast.makeText(context, "Impossible de créer le fichier image.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // La permission n'est pas accordée, on la demande
                                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            }
                        )
                    }
                }
            }

            // Informations personnelles
            Text(
                text = "Informations personnelles",
                fontSize = 16.sp, // Légèrement réduit
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Première ligne : Nom et Prénom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacement réduit
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nom",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prénom",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true
                    )
                }
            }

            // Deuxième ligne : Poids, Taille
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacement réduit
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Poids",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = poids,
                        onValueChange = { poids = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "Kg",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Taille",
                        fontSize = 12.sp, // Réduit
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    OutlinedTextField(
                        value = taille,
                        onValueChange = { taille = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "Cm",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }

                //date de naissance

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true }, // Ouvre le dialogue au clic
                ) {
                    // Label pour le champ
                    Text(
                        text = "Date de naissance",fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Box {

                        // Champ de texte cliquable qui affiche la date
                        OutlinedTextField(
                            value = dateDeNaissance?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                ?: "", onValueChange = { /* Ne fait rien car readOnly */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }, // Ouvre le dialogue au clic
                            readOnly = true, // Empêche la saisie au clavier
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp),
                            placeholder = {
                                Text(
                                    modifier = Modifier.clickable { showDatePicker = true },
                                    text = "JJ/MM/AAAA",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize() // Prend exactement la même taille que le OutlinedTextField
                                .clickable(
                                    onClick = { showDatePicker = true },
                                    // Enlève l'ondulation visuelle du clic si vous le souhaitez
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        )
                    }
                }
            }

            // Menu déroulant Sexe
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sexe",
                    fontSize = 12.sp, // Réduit
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = sexeExpanded,
                    onExpandedChange = { sexeExpanded = !sexeExpanded }
                ) {
                    OutlinedTextField(
                        value = sexe.libelle,
                        onValueChange = {sexe = sexe},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        placeholder = {
                            Text(
                                "Sélectionner",
                                color = Color.Gray,
                                fontSize = 12.sp // Réduit
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexeExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = sexeExpanded,
                        onDismissRequest = { sexeExpanded = false }
                    ) {
                        Sexe.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.libelle, fontSize = 14.sp) },
                                onClick = {
                                    sexe = option
                                    sexeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Menu déroulant Niveau d'expérience
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Niveau d'expérience",
                    fontSize = 12.sp, // Réduit
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = niveauExpanded,
                    onExpandedChange = { niveauExpanded = !niveauExpanded }
                ) {
                    OutlinedTextField(
                        value = niveau.libelle,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        placeholder = {
                            Text(
                                "Sélectionner",
                                color = Color.Gray,
                                fontSize = 12.sp // Réduit
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = niveauExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = niveauExpanded,
                        onDismissRequest = { niveauExpanded = false }
                    ) {
                        NiveauExperience.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.libelle, fontSize = 14.sp) },
                                onClick = {
                                    niveau = option
                                    niveauExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Section Jours d'entraînement
            Text(
                text = "Jours d'entraînement préférés",
                fontSize = 14.sp, // Réduit
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // --- Section Jours d'entraînement ---
            DayCheckboxGrid(
                joursSelectionnes = joursSelectionnes,
                onDaySelectionChanged = { day, isChecked ->
                    joursSelectionnes = if (isChecked) {
                        joursSelectionnes + day
                    } else {
                        joursSelectionnes - day
                    }
                }
            )

                // Section Informations optionnelles
                Text(
                    text = "Informations optionnelles",
                    fontSize = 14.sp, // Réduit
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Champ Poids cible
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Poids cible",
                            fontSize = 12.sp, // Réduit
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = " (optionnel)",
                            fontSize = 10.sp, // Réduit
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    OutlinedTextField(
                        value = poidsCible,
                        onValueChange = { poidsCible = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(fontSize = 14.sp), // Réduit
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "Kg",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }

                // Ligne VMA, FCM, FCR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacement réduit
                ) {
                    // VMA
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VMA",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = " (opt)",
                                fontSize = 10.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        OutlinedTextField(
                            value = vma,
                            onValueChange = { vma = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp), // Réduit
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // FCM
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FCM",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = " (opt)",
                                fontSize = 10.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        OutlinedTextField(
                            value = fcm,
                            onValueChange = { fcm = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp), // Réduit
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // FCR
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FCR",
                                fontSize = 12.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = " (opt)",
                                fontSize = 10.sp, // Réduit
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        OutlinedTextField(
                            value = fcr,
                            onValueChange = { fcr = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            textStyle = TextStyle(fontSize = 14.sp), // Réduit
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // Espace supplémentaire en bas pour le scroll
                Spacer(modifier = Modifier.height(16.dp))
            }


        // Bouton Suivant
        Button(
            onClick = {
                if (isButtonEnabled) {
                    if(isEditMode && viewModel != null){
                        viewModel.updateUtilisateur(Utilisateur(
                            id = utilisateurId,
                            nom = nom,
                            prenom = prenom,
                            imageUri = imageUri?.toString(),
                            dateDeNaissance = dateDeNaissance,
                            sexe = sexe,
                            poids = poids.toDouble(),
                            poidsCible = poidsCible.toDouble(),
                            taille = taille.toInt(),
                            vma = vma.toDoubleOrNull(),
                            fcm = fcm.toIntOrNull(),
                            fcr = fcr.toIntOrNull(),
                            niveauExperience = niveau,
                            joursEntrainementDisponibles = joursSelectionnes.toList() // On convertit le Set en List


                        ))
                    }else {
                        saveUser()
                    }
                    onNext()
                } else {
                    Toast.makeText(context, "Veuillez remplir tous les champs obligatoires.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF751F), disabledContainerColor = Color.LightGray)
        ) {
            Text("Sauvegarder", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Bouton suppression utilisateur
        /**
        Button(
            onClick = {
                suppUser()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF751F), disabledContainerColor = Color.LightGray)
        ) {
            Text("supp", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        **/
    }


    // --- Dialogue DatePicker ---
    if (showDatePicker) {
        DatePickerComponent(
            initialDate = dateDeNaissance,
            onDateSelected = { newDate ->
                dateDeNaissance = newDate

            },
            onDismiss = { showDatePicker = false } // Cache le dialogue
        )
    }
    }
private fun isFormValid(
    nom: String?,
    prenom: String?,
    dateDeNaissance: LocalDate?,
    poids: Double?,
    taille: Int?,
    joursEntrainementDisponibles: List<JourSemaine>?
): Boolean {
    if(
        nom.isNullOrBlank() ||
        prenom.isNullOrBlank() ||
        dateDeNaissance == null ||
        poids == null ||
        taille == null ||
        joursEntrainementDisponibles.isNullOrEmpty()
    ){
        return false
    }

    val ageAsInt =  LocalDate.now().year - dateDeNaissance.year
    return  nom.isNotBlank() &&
            prenom.isNotBlank() &&
            ageAsInt in 1..100 &&
            poids > 0 &&
            taille > 0 &&
            joursEntrainementDisponibles.isNotEmpty()
}



// Composant réutilisable pour les checkboxes des jours
@Composable
fun DayCheckbox(
    day: JourSemaine,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp) // Taille réduite
        )
        Text(
            text = day.name.lowercase().replaceFirstChar { it.titlecase() },
            fontSize = 12.sp, // Réduit
            color = Color.Black,
            modifier = Modifier.padding(start = 2.dp) // Espacement réduit
        )
    }
}

/**
 * Un composant qui affiche une grille de checkboxes pour les jours de la semaine,
 * le tout encadré dans une Card pour une meilleure présentation visuelle.
 */
@Composable
fun DayCheckboxGrid(
    joursSelectionnes: Set<JourSemaine>,
    onDaySelectionChanged: (JourSemaine, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Jours d'entraînement préférés",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // La logique de boucle pour générer les checkboxes
            val jours = JourSemaine.entries.chunked(3) // Divise les 7 jours en groupes de 3
            jours.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    rowItems.forEach { day ->
                        Box(modifier = Modifier.weight(1f)) {
                            DayCheckbox(
                                day = day,
                                checked = joursSelectionnes.contains(day),
                                onCheckedChange = { isChecked ->
                                    onDaySelectionChanged(day, isChecked)
                                }
                            )
                        }
                    }
                    // Ajoute des boîtes vides pour que la dernière ligne soit alignée
                    if (rowItems.size < 3) {
                        Spacer(modifier = Modifier.weight((3 - rowItems.size).toFloat()))
                    }
                }
            }
        }
    }
}

/**
 * Crée une entrée vide dans le MediaStore (Galerie "Pictures" du téléphone)
 * et retourne son URI. Cette URI sera utilisée par l'application Appareil Photo
 * pour sauvegarder l'image dans un stockage permanent.
 *
 * @param context Le contexte de l'application.
 * @return L'Uri de la future image.
 */
fun createImageUriForCamera(context: Context): Uri? {
    val resolver = context.contentResolver

    // Le nom du fichier sera basé sur la date et l'heure actuelles
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "PLLRUN_PROFILE_$timeStamp.jpg"

    // ContentValues contient les métadonnées de l'image
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        // Indique que le fichier doit être placé dans le répertoire Pictures
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PLL-Run")
        }
    }

    // Crée l'entrée dans la collection d'images du MediaStore
    // Pour Android Q (API 29) et supérieur, on utilise la collection externe.
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        // Pour les versions plus anciennes, on utilise la collection externe standard.
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    // Insère la nouvelle entrée et retourne son URI
    return resolver.insert(collection, contentValues)
}



