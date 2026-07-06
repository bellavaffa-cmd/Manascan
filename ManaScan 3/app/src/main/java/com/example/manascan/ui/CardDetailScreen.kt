package com.example.manascan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.manascan.R
import com.example.manascan.data.ScryfallCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    card: ScryfallCard,
    onScanAnother: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card.name) },
                navigationIcon = {
                    IconButton(onClick = onScanAnother) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.scan_title))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = card.displayImageUrl,
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(63f / 88f)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(Modifier.padding(top = 20.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(card.name, style = MaterialTheme.typography.titleLarge)
                    card.displayManaCost?.let { manaCost ->
                        Text(manaCost, style = MaterialTheme.typography.titleMedium)
                    }
                }

                card.displayTypeLine?.let { typeLine ->
                    Text(
                        typeLine,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                card.displayOracleText?.let { oracleText ->
                    Text(oracleText, style = MaterialTheme.typography.bodyLarge)
                }

                card.flavorText?.let { flavorText ->
                    Text(
                        flavorText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (card.power != null && card.toughness != null) {
                    Text(
                        "${card.power} / ${card.toughness}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                card.loyalty?.let { loyalty ->
                    Text(
                        "Loyalty: $loyalty",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Set", style = MaterialTheme.typography.labelLarge)
                        Text(card.setName.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("Rarity", style = MaterialTheme.typography.labelLarge)
                        Text(card.rarity.orEmpty().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium)
                    }
                    card.prices?.usd?.let { usd ->
                        Column {
                            Text("Price (USD)", style = MaterialTheme.typography.labelLarge)
                            Text("$$usd", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.attribution),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}
