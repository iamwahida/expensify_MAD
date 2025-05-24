package com.example.expensify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.expensify.ui.auth.LoginScreen
import com.example.expensify.ui.auth.SignupScreen
import com.example.expensify.ui.expenses.*
import com.example.expensify.ui.navigation.Destinations
import com.example.expensify.ui.theme.ExpensifyTheme
import com.example.expensify.ui.trips.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ExpensifyTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = Destinations.LOGIN) {

                    composable(Destinations.LOGIN) {
                        LoginScreen(navController)
                    }

                    composable(Destinations.SIGNUP) {
                        SignupScreen(navController)
                    }

                    composable(Destinations.MAIN) {
                        AllTripsScreen(
                            onTripSelected = { trip ->
                                navController.navigate("${Destinations.VIEW_EXPENSES}/${trip.id}/${trip.name}")
                            }
                        )
                    }

                    composable(Destinations.CREATE_TRIP) {
                        CreateTripScreen(onTripCreated = {
                            navController.popBackStack()
                        })
                    }

                    composable(
                        route = "${Destinations.VIEW_EXPENSES}/{tripId}/{tripName}",
                        arguments = listOf(
                            navArgument("tripId") { type = NavType.StringType },
                            navArgument("tripName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                        val tripName = backStackEntry.arguments?.getString("tripName") ?: ""
                        ViewAllExpensesScreen(tripId = tripId, tripName = tripName)
                    }

                    composable(
                        route = "${Destinations.ADD_EXPENSE}/{tripId}",
                        arguments = listOf(navArgument("tripId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                        AddExpenseScreen(tripId = tripId, members = emptyList()) {
                            navController.popBackStack()
                        }
                    }

                    composable(Destinations.TRIPS) {
                        AllTripsScreen(
                            onTripSelected = { trip ->
                                navController.navigate("${Destinations.VIEW_EXPENSES}/${trip.id}/${trip.name}")
                            }
                        )
                    }
                }
            }
        }
    }
}
