package com.arafat1419.vika

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arafat1419.vika.ui.components.HomeTopBar
import com.arafat1419.vika.ui.components.InfiniteSlider
import com.arafat1419.vika.ui.components.JKNBottomNavigationBar
import com.arafat1419.vika.ui.components.MenuGrid
import com.arafat1419.vika.ui.components.MenuLainnyaContent
import com.arafat1419.vika.ui.components.QueueCard
import com.arafat1419.vika.ui.components.UserGreetingCard
import com.arafat1419.vika.ui.theme.VIKATheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingIntent = intent

        setContent {
            VIKATheme {
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
            "berita" -> navController.navigate("berita")
            "kartu" -> navController.navigate("kartu")
            "faq" -> navController.navigate("faq")
            "profile" -> navController.navigate("profile")
        }
    }
}

@Composable
fun SampleApp(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentRoute != "menu_lainnya") {
                JKNBottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    snackbarHostState = snackbarHostState,
                    navController = navController
                )
            }
            composable("berita") {
                BeritaScreen()
            }
            composable("kartu") {
                KartuScreen()
            }
            composable("faq") {
                FAQScreen()
            }
            composable("profile") {
                ProfileScreen()
            }
            composable("menu_lainnya") {
                MenuLainnyaScreen(
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HomeTopBar(
            onNotificationClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Notifikasi clicked")
                }
            },
            onCSClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Customer Service clicked")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            UserGreetingCard(
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            QueueCard(
                onTakeQueueClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ambil Antrian clicked")
                    }
                }
            )

            Spacer(modifier = Modifier.padding(12.dp))

            MenuGrid(
                onMenuClick = { menuItem ->
                    if (menuItem.id == 12) {
                        navController.navigate("menu_lainnya")
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("${menuItem.title} clicked")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(12.dp))

            InfiniteSlider()

            Spacer(modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun BeritaScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.berita_title),
        description = stringResource(R.string.berita_description)
    )
}

@Composable
fun KartuScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.kartu_title),
        description = stringResource(R.string.kartu_description)
    )
}

@Composable
fun FAQScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.faq_title),
        description = stringResource(R.string.faq_description)
    )
}

@Composable
fun ProfileScreen() {
    PlaceholderScreen(
        title = stringResource(R.string.profile_title),
        description = stringResource(R.string.profile_description)
    )
}

@Composable
fun MenuLainnyaScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    MenuLainnyaContent(
        onMenuClick = { menuItem ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar("${menuItem.title} clicked")
            }
        },
        onEditClick = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Edit clicked")
            }
        },
        onBackClick = {
            navController.navigateUp()
        }
    )
}

@Composable
fun PlaceholderScreen(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
