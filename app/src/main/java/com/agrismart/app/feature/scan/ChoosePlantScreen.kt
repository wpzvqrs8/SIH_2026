package com.agrismart.app.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agrismart.app.R
import com.agrismart.app.data.repo.CropRepository
import com.agrismart.app.domain.model.Crop

import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun ChoosePlantScreen(
    cropRepository: CropRepository,
    onCropSelected: (String) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val crops = remember { cropRepository.getOfflineCrops() }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCrops = remember(searchQuery, crops) {
        if (searchQuery.isBlank()) crops else crops.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Back to Home",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.choose_plant_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search plant class (e.g. Wheat, Tomato, Cotton...)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCrops) { crop ->
                CropTile(
                    crop = crop,
                    onClick = { onCropSelected(crop.id) }
                )
            }
        }
    }
}

fun getCropEmoji(cropId: String): String {
    return when (cropId.lowercase()) {
        "apple" -> "🍎"
        "ash_gourd" -> "🍈"
        "banana" -> "🍌"
        "bean" -> "🫘"
        "bell_pepper" -> "🫑"
        "betel" -> "🍃"
        "bitter_gourd" -> "🥒"
        "black_gram" -> "🌱"
        "blueberry" -> "🫐"
        "bottle_gourd" -> "🍈"
        "brinjal_eggplant" -> "🍆"
        "cabbage" -> "🥬"
        "cashew" -> "🥜"
        "cauliflower" -> "🥦"
        "cherry" -> "🍒"
        "chilli" -> "🌶️"
        "citrus_orange" -> "🍊"
        "coconut" -> "🥥"
        "coffee" -> "☕"
        "corn_maize", "corn" -> "🌽"
        "cotton" -> "☁️"
        "cowpea" -> "🌱"
        "cucumber" -> "🥒"
        "custard_apple" -> "🍈"
        "finger_millet_ragi" -> "🌾"
        "garlic" -> "🧄"
        "ginger" -> "🫚"
        "grape" -> "🍇"
        "groundnut" -> "🥜"
        "guava" -> "🍐"
        "jamun" -> "🫐"
        "jute" -> "🌱"
        "lemon" -> "🍋"
        "lentil" -> "🫘"
        "malabar_spinach" -> "🥬"
        "mango" -> "🥭"
        "moringa" -> "🌿"
        "okra" -> "🫛"
        "onion" -> "🧅"
        "papaya" -> "🥭"
        "peach" -> "🍑"
        "pomegranate" -> "🍎"
        "potato" -> "🥔"
        "pumpkin" -> "🎃"
        "radish" -> "🧅"
        "raspberry" -> "🫐"
        "rice" -> "🌾"
        "soybean" -> "🌱"
        "spinach" -> "🥬"
        "squash" -> "🥒"
        "strawberry" -> "🍓"
        "sugarcane" -> "🎋"
        "sunflower" -> "🌻"
        "tea" -> "🍵"
        "tobacco" -> "🍂"
        "tomato" -> "🍅"
        "turmeric" -> "🫚"
        "watermelon" -> "🍉"
        "wheat" -> "🌾"
        else -> "🪴"
    }
}

@Composable
fun CropTile(
    crop: Crop,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getCropEmoji(crop.id),
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = crop.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (!crop.coverageNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = crop.coverageNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
