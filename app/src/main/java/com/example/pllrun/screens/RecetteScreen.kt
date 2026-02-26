package com.example.pllrun.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pllrun.Classes.Recette
import com.example.pllrun.InventaireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetteScreen(
    viewModel: InventaireViewModel, // Suppose que le ViewModel gère l'accès aux recettes
    onNavigateBack: () -> Unit,
    onAddRecette: () -> Unit // Pour naviguer vers un écran de création de recette
) {
    // Récupère la liste des recettes depuis le ViewModel
    // Note : Il faudra ajouter la logique pour getToutesLesRecettes() dans le ViewModel
    val recettes by viewModel.getAllRecettesInventaire().collectAsState(initial = emptyList<Recette>())
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mes Recettes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Vous pouvez ajouter une icône de retour si nécessaire
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecette,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une recette")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (recettes.isEmpty()) {
                item {
                    Text(
                        text = "Aucune recette enregistrée. Appuyez sur '+' pour en ajouter une.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(recettes) { recette ->
                    RecetteCard(
                        recette = recette,
                        isExpanded = expandedCardId == recette.id,
                        onClick = {
                            expandedCardId = if (expandedCardId == recette.id) null else recette.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecetteCard(recette: Recette, isExpanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Rend la carte entière cliquable
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // --- Ligne du haut (Header) ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = "Icône de recette",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recette.titre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NutrimentItem(label = "Cal", value = recette.calories.toInt().toString())
                        NutrimentItem(label = "Pro", value = "${recette.proteines.toInt()}g")
                        NutrimentItem(label = "Glu", value = "${recette.glucides.toInt()}g")
                        NutrimentItem(label = "Lip", value = "${recette.lipides.toInt()}g")
                    }
                }

                // Icône pour indiquer si la carte est développable/réduite
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Réduire" else "Développer",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // --- Section développable avec le texte de la recette ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = recette.texte,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp // Améliore la lisibilité
                    )
                }
            }
        }
    }
}

@Composable
fun NutrimentItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
