package com.example.currency_exchange_project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.currencysdk.models.FavoriteItem
import com.example.currencysdk.models.HistoryItem
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * FavoritesHistoryScreen shows two sections:
 *  - Favorites  (Create / Read / Delete)
 *  - History    (Read only — the backend creates entries automatically)
 *
 * A single LazyColumn is used so the whole screen scrolls together and
 * the list doesn't grow past the screen without scrolling.
 */
@Composable
fun FavoritesHistoryScreen(
    modifier: Modifier = Modifier,
    vm: CurrencyViewModel = viewModel()
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // ── Back button ───────────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { vm.navigateBack() }) {
                Text("← Back to Converter")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Loading indicator ─────────────────────────────────────────────────
        if (vm.isLoadingFavoritesHistory) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading…")
                }
            }
            return@LazyColumn
        }

        // ─────────────────────────────────────────────────────────────────────
        // FAVORITES SECTION
        // ─────────────────────────────────────────────────────────────────────
        item {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        if (vm.favoritesList.isEmpty()) {
            item {
                Text(
                    text = "No favorites saved yet. Press ★ on the converter screen to save one.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(
                items = vm.favoritesList,
                key = { it.id }
            ) { favorite ->
                FavoriteRow(
                    favorite = favorite,
                    onUse = { vm.useFavorite(it) },
                    onDelete = { vm.deleteFavorite(it.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // ─────────────────────────────────────────────────────────────────────
        // HISTORY SECTION
        // ─────────────────────────────────────────────────────────────────────
        item {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        if (vm.historyList.isEmpty()) {
            item {
                Text(
                    text = "No conversions yet. Use the converter and history will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(vm.historyList) { historyItem ->
                HistoryRow(item = historyItem)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * A single row in the Favorites list.
 *
 * Shows only the currency pair plus two action buttons:
 *  - Use    → fills the pair in the converter and navigates back
 *  - Delete → removes the favorite from MongoDB and the list
 */
@Composable
fun FavoriteRow(
    favorite: FavoriteItem,
    onUse: (FavoriteItem) -> Unit,
    onDelete: (FavoriteItem) -> Unit
) {
    val from = favorite.fromCurrency.ifEmpty { "—" }
    val to = favorite.toCurrency.ifEmpty { "—" }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$from → $to",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            TextButton(onClick = { onUse(favorite) }) {
                Text("Use")
            }

            OutlinedButton(onClick = { onDelete(favorite) }) {
                Text("Delete")
            }
        }
        HorizontalDivider()
    }
}

/**
 * A single row in the History list.
 *
 * Shows amount, converted amount, rate, and a formatted timestamp.
 * History is read-only — no action buttons.
 */
@Composable
fun HistoryRow(item: HistoryItem) {
    val from = item.fromCurrency.ifEmpty { "—" }
    val to = item.toCurrency.ifEmpty { "—" }
    val createdAt = item.createdAt.ifEmpty { "—" }
    val formattedTime = formatTimestamp(createdAt)

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "${item.amount} $from = ${item.convertedAmount} $to",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Rate: 1 $from = ${item.rate} $to",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodySmall
        )
    }
    HorizontalDivider()
}

/**
 * Converts backend UTC/ISO timestamp into the device's local timezone
 * and formats it into a short readable string.
 *
 * Example:
 * 2026-04-18T16:12:42.146733+00:00  ->  18/04/2026 19:12   (if device is in Israel time)
 */
fun formatTimestamp(timestamp: String): String {
    return try {
        val parsed = OffsetDateTime.parse(timestamp)
        val localTime = parsed.atZoneSameInstant(ZoneId.systemDefault())
        localTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        timestamp
    }
}