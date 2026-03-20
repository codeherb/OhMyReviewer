package io.yogiyo.ohmyreviewer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.yogiyo.ohmyreviewer.ui.aitextreview.AiTextReviewScreen
import io.yogiyo.ohmyreviewer.ui.image.ImageScreen
import io.yogiyo.ohmyreviewer.ui.review.ReviewScreen

@Composable
fun OhMyReviewerNavHost(
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        bottomBar = {
            OhMyReviewerBottomBar(navController = navController)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.IMAGE,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.IMAGE) {
                ImageScreen()
            }

            composable(Route.REVIEW) {
                ReviewScreen()
            }

            composable(Route.AI_TEXT_REVIEW) {
                AiTextReviewScreen()
            }
        }
    }
}

@Composable
private fun OhMyReviewerBottomBar(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(text = tab.label) },
            )
        }
    }
}
