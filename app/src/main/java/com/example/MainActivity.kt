package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.GameContainer
import com.example.ui.components.SettingsCheatDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full-bleed immersive status and navigation bars content placement
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                var isSettingsOpen by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    GameContainer(
                        viewModel = gameViewModel,
                        onOpenSettings = { isSettingsOpen = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )

                    if (isSettingsOpen) {
                        SettingsCheatDialog(
                            viewModel = gameViewModel,
                            onDismiss = { isSettingsOpen = false }
                        )
                    }
                }
            }
        }
    }
}
