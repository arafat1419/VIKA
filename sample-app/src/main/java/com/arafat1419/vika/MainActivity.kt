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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
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
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.VikaDisplayMode
import com.vika.sdk.models.VikaUIOptions
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
            "info_program_jkn" -> navController.navigate("info_program_jkn")
            "telehealth" -> navController.navigate("telehealth")
            "info_riwayat_pelayanan" -> navController.navigate("info_riwayat_pelayanan")
            "bugar" -> navController.navigate("bugar")
            "new_rehap" -> navController.navigate("new_rehap")
            "penambahan_peserta" -> navController.navigate("penambahan_peserta")
            "info_peserta" -> navController.navigate("info_peserta")
            "sos" -> navController.navigate("sos")
            "info_lokasi_faskes" -> navController.navigate("info_lokasi_faskes")
            "perubahan_data_peserta" -> navController.navigate("perubahan_data_peserta")
            "pengaduan_layanan_jkn" -> navController.navigate("pengaduan_layanan_jkn")
            "skrining_riwayat_kesehatan" -> navController.navigate("skrining_riwayat_kesehatan")
            "pendaftaran_pelayanan" -> navController.navigate("pendaftaran_pelayanan")
            "info_ketersediaan_tempat_tidur" -> navController.navigate("info_ketersediaan_tempat_tidur")
            "info_jadwal_tindakan_operasi" -> navController.navigate("info_jadwal_tindakan_operasi")
            "info_iuran" -> navController.navigate("info_iuran")
            "pendaftaran_auto_debit" -> navController.navigate("pendaftaran_auto_debit")
            "info_riwayat_pembayaran" -> navController.navigate("info_riwayat_pembayaran")
            "info_virtual_account" -> navController.navigate("info_virtual_account")
            "minum_obat" -> navController.navigate("minum_obat")
            "tren_penyakit_daerah" -> navController.navigate("tren_penyakit_daerah")
            "antrean_online" -> navController.navigate("antrean_online")
        }
    }
}

@Composable
fun SampleApp(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sdk = VikaSDK.getInstance()

    // Define main screens that should show bottom navigation
    val mainScreens = listOf("home", "berita", "kartu", "faq", "profile")

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentRoute in mainScreens) {
                JKNBottomNavigationBar(navController = navController)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val options = VikaUIOptions.builder()
                            .displayMode(VikaDisplayMode.DIALOG)
                            .appLogo(R.mipmap.ic_launcher)
                            .appTitle("Sample App")
                            .dismissOnTouchOutside(true)
                            .build()
                        sdk.openVikaSDK(context, options)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Open Voice Assistant",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
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
                BeritaScreen(navController)
            }
            composable("kartu") {
                KartuScreen(navController)
            }
            composable("faq") {
                FAQScreen(navController)
            }
            composable("profile") {
                ProfileScreen(navController)
            }
            composable("menu_lainnya") {
                MenuLainnyaScreen(
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
            composable("info_program_jkn") {
                PlaceholderScreen(
                    title = "Info Program JKN",
                    description = "Menu ini berisi rangkuman menyeluruh mengenai Program Jaminan Kesehatan Nasional (JKN)",
                    navController = navController
                )
            }
            composable("telehealth") {
                PlaceholderScreen(
                    title = "Telehealth",
                    description = "Konsultasi kesehatan jarak jauh dengan tenaga medis yang terlatih",
                    navController = navController
                )
            }
            composable("info_riwayat_pelayanan") {
                PlaceholderScreen(
                    title = "Info Riwayat Pelayanan",
                    description = "Daftar lengkap pelayanan kesehatan yang pernah diterima peserta",
                    navController = navController
                )
            }
            composable("bugar") {
                PlaceholderScreen(
                    title = "Bugar",
                    description = "Konten edukatif mengenai gaya hidup sehat dan kebugaran",
                    navController = navController
                )
            }
            composable("new_rehap") {
                PlaceholderScreen(
                    title = "Cicilan REHAP",
                    description = "Simulasi dan pengaturan pembayaran tunggakan iuran melalui skema REHAP",
                    navController = navController
                )
            }
            composable("penambahan_peserta") {
                PlaceholderScreen(
                    title = "Penambahan Peserta",
                    description = "Menambahkan anggota keluarga ke dalam kepesertaan JKN",
                    navController = navController
                )
            }
            composable("info_peserta") {
                PlaceholderScreen(
                    title = "Info Peserta",
                    description = "Informasi lengkap mengenai status kepesertaan Anda",
                    navController = navController
                )
            }
            composable("sos") {
                PlaceholderScreen(
                    title = "SOS",
                    description = "Akses cepat untuk kondisi darurat dan bantuan medis",
                    navController = navController
                )
            }
            composable("info_lokasi_faskes") {
                PlaceholderScreen(
                    title = "Info Lokasi Faskes",
                    description = "Daftar lengkap fasilitas kesehatan yang bekerja sama dengan BPJS Kesehatan",
                    navController = navController
                )
            }
            composable("perubahan_data_peserta") {
                PlaceholderScreen(
                    title = "Perubahan Data Peserta",
                    description = "Memperbarui informasi kepesertaan seperti alamat, identitas, dan faskes pilihan",
                    navController = navController
                )
            }
            composable("pengaduan_layanan_jkn") {
                PlaceholderScreen(
                    title = "Pengaduan Layanan JKN",
                    description = "Menyampaikan keluhan terkait pelayanan kesehatan dan proses administrasi",
                    navController = navController
                )
            }
            composable("skrining_riwayat_kesehatan") {
                PlaceholderScreen(
                    title = "Skrining Riwayat Kesehatan",
                    description = "Skrining mandiri untuk mendeteksi potensi risiko penyakit kronis",
                    navController = navController
                )
            }
            composable("pendaftaran_pelayanan") {
                PlaceholderScreen(
                    title = "Pendaftaran Pelayanan",
                    description = "Antrean online untuk mendaftar layanan di fasilitas kesehatan",
                    navController = navController
                )
            }
            composable("info_ketersediaan_tempat_tidur") {
                PlaceholderScreen(
                    title = "Info Ketersediaan Tempat Tidur",
                    description = "Informasi terbaru mengenai jumlah tempat tidur kosong di fasilitas kesehatan rujukan",
                    navController = navController
                )
            }
            composable("info_jadwal_tindakan_operasi") {
                PlaceholderScreen(
                    title = "Info Jadwal Tindakan Operasi",
                    description = "Informasi jadwal tindakan operasi yang telah terdaftar di fasilitas kesehatan",
                    navController = navController
                )
            }
            composable("info_iuran") {
                PlaceholderScreen(
                    title = "Info Iuran",
                    description = "Rangkuman lengkap mengenai besaran iuran dan ketentuan pembayaran",
                    navController = navController
                )
            }
            composable("pendaftaran_auto_debit") {
                PlaceholderScreen(
                    title = "Pendaftaran Auto Debit",
                    description = "Mengaktifkan fitur auto debit untuk pembayaran iuran otomatis",
                    navController = navController
                )
            }
            composable("info_riwayat_pembayaran") {
                PlaceholderScreen(
                    title = "Info Riwayat Pembayaran",
                    description = "Catatan seluruh pembayaran iuran yang telah dilakukan",
                    navController = navController
                )
            }
            composable("info_virtual_account") {
                PlaceholderScreen(
                    title = "Info Virtual Account",
                    description = "Informasi nomor virtual account untuk pembayaran iuran",
                    navController = navController
                )
            }
            composable("minum_obat") {
                PlaceholderScreen(
                    title = "Minum Obat",
                    description = "Pengingat otomatis untuk jadwal konsumsi obat",
                    navController = navController
                )
            }
            composable("tren_penyakit_daerah") {
                PlaceholderScreen(
                    title = "Tren Penyakit Daerah",
                    description = "Data statistik mengenai tren penyakit di wilayah tertentu",
                    navController = navController
                )
            }
            composable("antrean_online") {
                PlaceholderScreen(
                    title = "Antrean Online",
                    description = "Fitur antrean digital pada fasilitas kesehatan yang bekerja sama",
                    navController = navController
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
                    navController.navigate("antrean_online")
                }
            )

            Spacer(modifier = Modifier.padding(12.dp))

            MenuGrid(
                onMenuClick = { menuItem ->
                    when (menuItem.id) {
                        1 -> navController.navigate("info_program_jkn")
                        2 -> navController.navigate("telehealth")
                        3 -> navController.navigate("info_riwayat_pelayanan")
                        4 -> navController.navigate("bugar")
                        5 -> navController.navigate("new_rehap")
                        6 -> navController.navigate("penambahan_peserta")
                        7 -> navController.navigate("info_peserta")
                        8 -> navController.navigate("sos")
                        9 -> navController.navigate("info_lokasi_faskes")
                        10 -> navController.navigate("perubahan_data_peserta")
                        11 -> navController.navigate("pengaduan_layanan_jkn")
                        12 -> navController.navigate("menu_lainnya")
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
fun BeritaScreen(navController: NavHostController? = null) {
    PlaceholderScreen(
        title = stringResource(R.string.berita_title),
        description = stringResource(R.string.berita_description),
        navController = navController
    )
}

@Composable
fun KartuScreen(navController: NavHostController? = null) {
    PlaceholderScreen(
        title = stringResource(R.string.kartu_title),
        description = stringResource(R.string.kartu_description),
        navController = navController
    )
}

@Composable
fun FAQScreen(navController: NavHostController? = null) {
    PlaceholderScreen(
        title = stringResource(R.string.faq_title),
        description = stringResource(R.string.faq_description),
        navController = navController
    )
}

@Composable
fun ProfileScreen(navController: NavHostController? = null) {
    PlaceholderScreen(
        title = stringResource(R.string.profile_title),
        description = stringResource(R.string.profile_description),
        navController = navController
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
            when (menuItem.id) {
                1 -> navController.navigate("info_program_jkn")
                2 -> navController.navigate("telehealth")
                3 -> navController.navigate("info_riwayat_pelayanan")
                4 -> navController.navigate("bugar")
                5 -> navController.navigate("new_rehap")
                6 -> navController.navigate("penambahan_peserta")
                7 -> navController.navigate("info_peserta")
                8 -> navController.navigate("sos")
                9 -> navController.navigate("info_lokasi_faskes")
                10 -> navController.navigate("perubahan_data_peserta")
                11 -> navController.navigate("pengaduan_layanan_jkn")
                13 -> navController.navigate("skrining_riwayat_kesehatan")
                14 -> navController.navigate("pendaftaran_pelayanan")
                15 -> navController.navigate("info_ketersediaan_tempat_tidur")
                16 -> navController.navigate("info_jadwal_tindakan_operasi")
                17 -> navController.navigate("info_iuran")
                18 -> navController.navigate("pendaftaran_auto_debit")
                19 -> navController.navigate("info_riwayat_pembayaran")
                20 -> navController.navigate("info_virtual_account")
                21 -> navController.navigate("minum_obat")
                22 -> navController.navigate("tren_penyakit_daerah")
                else -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("${menuItem.title} clicked")
                    }
                }
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
fun PlaceholderScreen(
    title: String,
    description: String,
    navController: NavHostController? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button at top-left
        navController?.let {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Center content
        Column(
            modifier = Modifier.align(Alignment.Center),
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
