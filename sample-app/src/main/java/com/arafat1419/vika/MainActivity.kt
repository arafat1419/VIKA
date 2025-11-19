package com.arafat1419.vika

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.VikaDisplayMode
import com.vika.sdk.models.VikaThemeConfig
import com.vika.sdk.models.VikaUIOptions

class MainActivity : ComponentActivity() {

    private var pendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingIntent = intent

        setContent {
            val navController = rememberNavController()

            SampleApp(navController)

            LaunchedEffect(Unit) {
                pendingIntent?.let {
                    handleIntent(it, navController)
                    pendingIntent = null
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingIntent = intent
    }

    private fun handleIntent(intent: Intent, navController: NavHostController) {
        val uri = intent.data ?: return
        Log.d("MainActivity", "Received deep link: $uri")

        when (uri.host) {
            "home" -> navController.navigate("home")
            "products" -> {
                val category = uri.getQueryParameter("category")
                navController.navigate("products${category?.let { "?category=$it" } ?: ""}")
            }

            "product" -> {
                uri.getQueryParameter("id")?.let {
                    navController.navigate("product/$it")
                }
            }

            "cart" -> navController.navigate("cart")
            "profile" -> navController.navigate("profile")
        }
    }
}

@Composable
fun SampleApp(navController: NavHostController) {
    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable("products?category={category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                ProductListScreen(navController, category)
            }
            composable("product/{id}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                ProductDetailScreen(navController, productId)
            }
            composable("cart") {
                CartScreen(navController)
            }
            composable("profile") {
                ProfileScreen(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val sdk = VikaSDK.getInstance()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Navigation Sample") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Manual navigation
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Manual Navigation",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate("home") }
                        ) {
                            Text("Home")
                        }

                        OutlinedButton(
                            onClick = { navController.navigate("products") }
                        ) {
                            Text("Products")
                        }

                        OutlinedButton(
                            onClick = { navController.navigate("cart") }
                        ) {
                            Text("Cart")
                        }
                    }
                }
            }

            // VIKA SDK Display Modes
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "VIKA Display Modes",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Fullscreen (default)
                    Button(
                        onClick = {
                            val options = VikaUIOptions.builder()
                                .displayMode(VikaDisplayMode.FULLSCREEN)
                                .appLogo(R.mipmap.ic_launcher)
                                .appTitle("Sample App")
                                .build()
                            sdk.openVikaSDK(context, options)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fullscreen")
                    }

                    // Dialog mode
                    Button(
                        onClick = {
                            val options = VikaUIOptions.builder()
                                .displayMode(VikaDisplayMode.DIALOG)
                                .appLogo(R.mipmap.ic_launcher)
                                .appTitle("Sample App")
                                .dismissOnTouchOutside(true)
                                .build()
                            sdk.openVikaSDK(context, options)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dialog")
                    }

                    // Bottom sheet mode
                    Button(
                        onClick = {
                            val options = VikaUIOptions.builder()
                                .displayMode(VikaDisplayMode.BOTTOM_SHEET)
                                .appLogo(R.mipmap.ic_launcher)
                                .appTitle("Sample App")
                                .dismissOnTouchOutside(true)
                                .build()
                            sdk.openVikaSDK(context, options)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bottom Sheet")
                    }
                }
            }

            // Custom Theme Demo
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Custom Theme",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Custom blue theme
                    OutlinedButton(
                        onClick = {
                            val customTheme = VikaThemeConfig(
                                primaryColor = 0xFF2196F3,  // Blue
                                secondaryColor = 0xFFFF5722,  // Deep Orange
                                backgroundColor = 0xFF121212,  // Dark gray
                                textColor = 0xFFFFFFFF,  // White
                                surfaceColor = 0xFF1E1E1E,  // Slightly lighter gray
                                waveformColor = 0xFF2196F3  // Blue
                            )
                            val options = VikaUIOptions.builder()
                                .displayMode(VikaDisplayMode.DIALOG)
                                .appLogo(R.mipmap.ic_launcher)
                                .appTitle("Custom Theme")
                                .theme(customTheme)
                                .build()
                            sdk.openVikaSDK(context, options)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Blue Theme Dialog")
                    }

                    // Light theme
                    OutlinedButton(
                        onClick = {
                            val options = VikaUIOptions.builder()
                                .displayMode(VikaDisplayMode.BOTTOM_SHEET)
                                .appTitle("Light Theme")
                                .theme(VikaThemeConfig.DEFAULT_LIGHT)
                                .build()
                            sdk.openVikaSDK(context, options)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Light Theme Bottom Sheet")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Home Screen",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavHostController, category: String?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products${category?.let { " - $it" } ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Product List",
                    style = MaterialTheme.typography.headlineLarge
                )
                category?.let {
                    Text(
                        text = "Category: $it",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavHostController, productId: Int) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Product Detail",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Product ID: $productId",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Shopping Cart",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "User Profile",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}