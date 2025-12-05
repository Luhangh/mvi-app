package com.pos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pos.feature.payment.screen.PaymentScreen
import com.pos.feature.pos.screen.POSScreen
import com.pos.feature.printer.screen.PrinterScreen
import com.pos.feature.scanner.screen.ScannerScreen
import com.pos.ui.theme.POSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化Timber日志
        // if (BuildConfig.DEBUG) {
        //     Timber.plant(Timber.DebugTree())
        // }

        setContent {
            POSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "pos"
                    ) {
                        composable("pos") {
                            POSScreen(
                                onNavigateToPayment = { cartId, totalAmount, items ->
                                    // 导航到支付屏幕
                                    navController.navigate("payment/$cartId/$totalAmount")
                                },
                                onNavigateToScanner = {
                                    navController.navigate("scanner")
                                }
                            )
                        }

                        composable("scanner") {
                            ScannerScreen(
                                onBarcodeScanned = { barcode ->
                                    // 扫码后返回POS屏幕
                                    navController.popBackStack()
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            "payment/{cartId}/{totalAmount}"
                        ) { backStackEntry ->
                            val cartId = backStackEntry.arguments?.getString("cartId") ?: ""
                            val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0

                            PaymentScreen(
                                orderId = cartId,
                                totalAmount = totalAmount,
                                items = emptyList(),
                                onNavigateToPrinting = {
                                    navController.navigate("printer/$cartId") {
                                        popUpTo("payment/$cartId/$totalAmount") { inclusive = true }
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("printer/{orderId}") { backStackEntry ->
                            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

                            PrinterScreen(
                                orderId = orderId,
                                onNavigateToOrderHistory = {
                                    navController.navigate("pos") {
                                        popUpTo("printer/$orderId") { inclusive = true }
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
