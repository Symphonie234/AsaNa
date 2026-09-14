package ph.asana.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ph.asana.app.ui.auth.AuthScreen
import ph.asana.app.ui.category.CategoryScreen
import ph.asana.app.ui.favorites.FavoritesScreen
import ph.asana.app.ui.home.HomeScreen
import ph.asana.app.ui.search.SearchScreen
import ph.asana.app.ui.service.ServiceDetailScreen
import ph.asana.app.ui.settings.SettingsScreen

private data class BottomNavTab(val route: String, val label: String, val icon: ImageVector)

private val BOTTOM_NAV_TABS = listOf(
    BottomNavTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomNavTab(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite),
    BottomNavTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun AsaNaNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in Routes.BOTTOM_NAV_ROUTES) {
                NavigationBar {
                    BOTTOM_NAV_TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onCategoryClick = { category ->
                        navController.navigate(Routes.category(category.slug, category.name))
                    },
                    onServiceClick = { service ->
                        navController.navigate(Routes.serviceDetail(service.slug))
                    },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onServiceClick = { service ->
                        navController.navigate(Routes.serviceDetail(service.slug))
                    },
                )
            }

            composable(Routes.CATEGORY) {
                CategoryScreen(
                    onServiceClick = { service ->
                        navController.navigate(Routes.serviceDetail(service.slug))
                    },
                )
            }

            composable(Routes.SERVICE_DETAIL) {
                ServiceDetailScreen(
                    onBack = { navController.popBackStack() },
                    onSignInRequired = { navController.navigate(Routes.AUTH) },
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onServiceClick = { service ->
                        navController.navigate(Routes.serviceDetail(service.slug))
                    },
                    onSignInClick = { navController.navigate(Routes.AUTH) },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onSignInClick = { navController.navigate(Routes.AUTH) })
            }

            composable(Routes.AUTH) {
                AuthScreen(
                    onSignedIn = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
