package com.turkcell.rencarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.turkcell.rencarapp.ui.navigation.RenCarNavHost
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RenCarAppTheme {
                RenCarNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
