package com.example.currency_exchange_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.currency_exchange_project.ui.theme.CurrencyExchangeProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyExchangeProjectTheme(darkTheme = false) {
                // One ViewModel instance shared by both screens.
                // viewModel() returns the same object as long as the Activity is alive,
                // so navigation state (showFavoritesAndHistory) stays in sync.
                val vm: CurrencyViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (vm.showFavoritesAndHistory) {
                        FavoritesHistoryScreen(
                            modifier = Modifier.padding(innerPadding),
                            vm = vm
                        )
                    } else {
                        CurrencyConverterScreen(
                            modifier = Modifier.padding(innerPadding),
                            vm = vm
                        )
                    }
                }
            }
        }
    }
}
