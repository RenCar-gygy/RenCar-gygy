package com.turkcell.rencarapp.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow

object ThemeManager {
    // null: Cihaz teması (System default)
    // true: Karanlık tema (Dark mode)
    // false: Aydınlık tema (Light mode)
    val isDarkMode = MutableStateFlow<Boolean?>(null)
}