// MainActivity.kt
package com.turkcell.rencarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.turkcell.rencarapp.ui.navigation.RenCarNavHost
import com.turkcell.rencarapp.ui.theme.RenCarAppTheme
import com.turkcell.rencarapp.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ThemeManager'daki durumu dinliyoruz. Eğer null ise cihazın kendi temasını kullanıyor.
            val isDarkThemeState by ThemeManager.isDarkMode.collectAsState()
            val useDarkTheme = isDarkThemeState ?: isSystemInDarkTheme()

            RenCarAppTheme(darkTheme = useDarkTheme) {
                RenCarNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}