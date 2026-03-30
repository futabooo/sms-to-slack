package com.futabooo.smstoslack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.futabooo.smstoslack.ui.navigation.MainScreen
import com.futabooo.smstoslack.ui.theme.SmstoslackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as SmsToSlackApplication).appContainer
        setContent {
            SmstoslackTheme {
                MainScreen(appContainer = appContainer)
            }
        }
    }
}
