package ph.asana.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ph.asana.app.ui.category.CategoryScreen
import ph.asana.app.ui.home.HomeScreen
import ph.asana.app.ui.search.SearchScreen
import ph.asana.app.ui.service.ServiceDetailScreen

@Composable
fun AsaNaNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
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
            ServiceDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
