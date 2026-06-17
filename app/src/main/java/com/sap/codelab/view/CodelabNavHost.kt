package com.sap.codelab.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sap.codelab.view.create.CreateMemoScreen
import com.sap.codelab.view.detail.ViewMemoScreen
import com.sap.codelab.view.home.HomeScreen
import com.sap.codelab.view.map.LatLng
import com.sap.codelab.view.map.MapPickerScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_CREATE = "create"
private const val ROUTE_DETAIL = "detail/{memoId}"
private const val ROUTE_MAP = "map?lat={lat}&lng={lng}"
private const val KEY_PICKED_LOCATION = "picked_location"

@Composable
internal fun CodelabNavHost(
    deepLinkMemoId: Long? = null,
    onDeepLinkHandled: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
) {
    LaunchedEffect(deepLinkMemoId) {
        deepLinkMemoId?.let { memoId ->
            navController.navigate("detail/$memoId")
            onDeepLinkHandled()
        }
    }

    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeScreen(
                onOpenMemo = { memoId -> navController.navigate("detail/$memoId") },
                onCreateMemo = { navController.navigate(ROUTE_CREATE) },
            )
        }

        composable(ROUTE_CREATE) { entry ->
            val picked by entry.savedStateHandle
                .getStateFlow<DoubleArray?>(KEY_PICKED_LOCATION, null)
                .collectAsStateWithLifecycle()
            CreateMemoScreen(
                pickedLocation = picked?.let { LatLng(it[0], it[1]) },
                onLocationConsumed = { entry.savedStateHandle[KEY_PICKED_LOCATION] = null },
                onPickLocation = { current ->
                    val query = if (current != null) "?lat=${current.latitude}&lng=${current.longitude}" else ""
                    navController.navigate("map$query")
                },
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(navArgument("memoId") { type = NavType.LongType }),
        ) { entry ->
            ViewMemoScreen(
                memoId = entry.arguments?.getLong("memoId") ?: -1L,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = ROUTE_MAP,
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("lng") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) { entry ->
            val lat = entry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = entry.arguments?.getString("lng")?.toDoubleOrNull()
            MapPickerScreen(
                initial = if (lat != null && lng != null) LatLng(lat, lng) else null,
                onConfirm = { picked ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        KEY_PICKED_LOCATION,
                        doubleArrayOf(picked.latitude, picked.longitude),
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
