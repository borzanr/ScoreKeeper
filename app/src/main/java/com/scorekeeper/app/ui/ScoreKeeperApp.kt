package com.scorekeeper.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scorekeeper.app.ui.home.HomeScreen
import com.scorekeeper.app.ui.intro.IntroScreen
import com.scorekeeper.app.ui.match.MatchScreen
import com.scorekeeper.app.ui.newmatch.NewMatchScreen
import com.scorekeeper.app.ui.settings.SettingsScreen

private object Routes {
    const val INTRO = "intro"
    const val HOME = "home"
    const val NEW_MATCH = "new"
    const val MATCH_ARG = "matchId"
    const val MATCH = "match/{$MATCH_ARG}"
    const val SETTINGS = "settings"

    fun match(matchId: Long) = "match/$matchId"
}

@Composable
fun ScoreKeeperApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.INTRO,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        composable(Routes.INTRO) {
            IntroScreen(
                // A abertura sai da pilha: voltar da tela inicial fecha o app.
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INTRO) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNewMatch = { navController.navigate(Routes.NEW_MATCH) },
                onOpenMatch = { navController.navigate(Routes.match(it)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.NEW_MATCH) {
            NewMatchScreen(
                onBack = { navController.popBackStack() },
                // A tela de nova partida sai da pilha: voltar da partida leva à lista.
                onCreated = { matchId ->
                    navController.navigate(Routes.match(matchId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }

        composable(
            route = Routes.MATCH,
            arguments = listOf(navArgument(Routes.MATCH_ARG) { type = NavType.LongType }),
        ) {
            MatchScreen(onBack = { navController.popBackStack(Routes.HOME, inclusive = false) })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
