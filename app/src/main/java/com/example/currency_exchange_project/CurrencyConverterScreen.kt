package com.example.currency_exchange_project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * The main Currency Converter screen.
 *
 * Layout (top to bottom):
 *  1. Title
 *  2. From / To currency dropdowns side by side
 *  3. Amount text field
 *  4. [Convert] button  +  [★] save-favorite button
 *  5. Error message (if any)
 *  6. Exchange rate + converted amount results
 *  7. [View Favorites & History] navigation button
 */
@Composable
fun CurrencyConverterScreen(
    modifier: Modifier = Modifier,
    vm: CurrencyViewModel = viewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "Currency Converter SDK",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Currency dropdowns ────────────────────────────────────────────────
        if (vm.isLoadingCurrencies) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading currencies…")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    CurrencyDropdown(
                        selectedCode = vm.fromCurrency,
                        options = vm.currencyList.map { it.code },
                        onSelected = { vm.onFromCurrencySelected(it) }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("To", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    CurrencyDropdown(
                        selectedCode = vm.toCurrency,
                        options = vm.currencyList.map { it.code },
                        onSelected = { vm.onToCurrencySelected(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Amount input ──────────────────────────────────────────────────────
        Text("Amount", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = vm.amount,
            onValueChange = { vm.onAmountChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Convert button + star button ──────────────────────────────────────
        // Both buttons sit in a Row. Convert gets most of the space; the star
        // button is compact and sits to the right.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Convert button
            Button(
                onClick = { vm.convert() },
                modifier = Modifier.weight(1f),
                enabled = !vm.isConverting && !vm.isLoadingCurrencies
            ) {
                if (vm.isConverting) {
                    CircularProgressIndicator()
                } else {
                    Text("Convert")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Star button — saves the current pair as a favorite
            OutlinedButton(
                onClick = { vm.saveFavorite() },
                enabled = !vm.isSavingFavorite
            ) {
                // ★ is a plain Unicode character; no icon dependency needed
                Text(if (vm.isSavingFavorite) "…" else "★")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Error message ─────────────────────────────────────────────────────
        vm.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Results ───────────────────────────────────────────────────────────
        val rate = vm.exchangeRate
        val result = vm.conversionResult
        if (rate != null && result != null) {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Exchange rate: 1 ${rate.from} = ${rate.rate} ${rate.to}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Converted amount: ${result.convertedAmount} ${result.to}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Navigate to second screen ─────────────────────────────────────────
        OutlinedButton(
            onClick = { vm.navigateToFavoritesHistory() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Favorites & History")
        }
    }
}

/**
 * A reusable dropdown for picking a currency code.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    selectedCode: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCode,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { code ->
                DropdownMenuItem(
                    text = { Text(code) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
