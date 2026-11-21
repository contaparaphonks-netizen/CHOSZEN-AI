package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import com.example.core.configureAppCheck
import com.example.ui.ChoszenAppRoot
import com.example.ui.theme.ChoszenEditAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModel, or Firebase call
        configureAppCheck(this, intent)

        // STEP 2: Edge-to-edge & Compose UI
        enableEdgeToEdge()
        setContent {
            ChoszenEditAiTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    ChoszenAppRoot()
                }
            }
        }
    }
}
