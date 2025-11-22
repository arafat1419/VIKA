package com.arafat1419.vika.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.arafat1419.vika.R
import com.arafat1419.vika.ui.theme.JKNBlue

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
) {
    data object Home : BottomNavItem(
        route = "home",
        icon = Icons.Filled.Home,
        labelRes = R.string.nav_home
    )

    data object Berita : BottomNavItem(
        route = "berita",
        icon = Icons.Filled.Article,
        labelRes = R.string.nav_berita
    )

    data object Kartu : BottomNavItem(
        route = "kartu",
        icon = Icons.Filled.CreditCard,
        labelRes = R.string.nav_kartu
    )

    data object FAQ : BottomNavItem(
        route = "faq",
        icon = Icons.Filled.Help,
        labelRes = R.string.nav_faq
    )

    data object Profile : BottomNavItem(
        route = "profile",
        icon = Icons.Filled.Person,
        labelRes = R.string.nav_profile
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Berita,
    BottomNavItem.Kartu,
    BottomNavItem.FAQ,
    BottomNavItem.Profile
)

@Composable
fun JKNBottomNavigationBar(
    navController: NavController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = {
                    Text(text = stringResource(item.labelRes))
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JKNBlue,
                    selectedTextColor = JKNBlue,
                    indicatorColor = JKNBlue.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
