package com.turkcell.rencarapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

enum class RenCarBottomBarTab(
    val destination: String,
    val label: String,
    val icon: ImageVector,
) {
    Map(RenCarDestination.Map, "Harita", Icons.Default.Map),
    History(RenCarDestination.RentalHistory, "Geçmiş", Icons.AutoMirrored.Filled.List),
    Wallet(RenCarDestination.Wallet, "Cüzdan", Icons.Default.AccountBalanceWallet),
    Profile(RenCarDestination.Profile, "Profil", Icons.Default.Person),
}

@Composable
fun RenCarBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (RenCarBottomBarTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        RenCarBottomBarTab.entries.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.destination } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}
