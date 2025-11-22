package com.arafat1419.vika

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.vika.sdk.models.VikaThemeConfig
import com.vika.sdk.models.VikaUIOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                            .appTitle(context.getString(R.string.app_name))
                            .dismissOnTouchOutside(true)
                            .theme(VikaThemeConfig.DEFAULT_LIGHT)
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
                InfoLokasiScreen(navController = navController)
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
                AmbilAntreanScreen(navController = navController)
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

// Data models for healthcare facilities
data class HealthcareFacility(
    val id: Int,
    val name: String,
    val ulasan: String,
    val type: String,
    val distance: Double,
    val address: String,
    val phone: String
)

// Dummy data for facilities
fun getDummyFacilities(): List<HealthcareFacility> {
    return listOf(
        HealthcareFacility(
            id = 1,
            name = "RSUD Kota Jakarta",
            ulasan = "4.5",
            type = "Non Rawat Inap",
            distance = 2.5,
            address = "Jl. Gatot Subroto No. 123, Jakarta Pusat",
            phone = "(021) 5551234"
        ),
        HealthcareFacility(
            id = 2,
            name = "Puskesmas Menteng",
            ulasan = "4.2",
            type = "Non Rawat Inap",
            distance = 3.8,
            address = "Jl. Menteng Raya No. 45, Jakarta Pusat",
            phone = "(021) 5555678"
        ),
        HealthcareFacility(
            id = 3,
            name = "Klinik Sehat Bersama",
            ulasan = "4.7",
            type = "Non Rawat Inap",
            distance = 1.2,
            address = "Jl. Sudirman No. 78, Jakarta Selatan",
            phone = "(021) 5559012"
        ),
        HealthcareFacility(
            id = 4,
            name = "RS Husada Medika",
            ulasan = "4.3",
            type = "Non Rawat Inap",
            distance = 5.1,
            address = "Jl. HR Rasuna Said No. 234, Jakarta Selatan",
            phone = "(021) 5553456"
        ),
        HealthcareFacility(
            id = 5,
            name = "Puskesmas Tanah Abang",
            ulasan = "4.0",
            type = "Non Rawat Inap",
            distance = 4.3,
            address = "Jl. Tanah Abang III No. 56, Jakarta Pusat",
            phone = "(021) 5557890"
        )
    )
}

// Info Lokasi Faskes Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoLokasiScreen(navController: NavHostController) {
    val facilities = remember { getDummyFacilities() }
    var showBottomSheet by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Box(modifier = Modifier.fillMaxSize()) {
        // Map placeholder area
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Info Lokasi Faskes",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Map placeholder (colored box with icon)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Map",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Peta Lokasi Faskes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bottom sheet with facility list
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Fasilitas Kesehatan Terdekat",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Scrollable facility list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                    ) {
                        facilities.forEach { facility ->
                            FacilityCard(facility = facility)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityCard(facility: HealthcareFacility) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Facility name
            Text(
                text = facility.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Top section: Ulasan | Type | Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐ ${facility.ulasan} Ulasan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999)
                )
                Text(
                    text = "🏥 ${facility.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999)
                )
                Text(
                    text = "📍 ${facility.distance} KM",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Address",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = facility.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = "Phone",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = facility.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333),
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Ambil Antrean / Antrean Online Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbilAntreanScreen(navController: NavHostController) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Form state
    var selectedPoli by remember { mutableStateOf("") }
    var expandedPoli by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf("") }
    var expandedSchedule by remember { mutableStateOf(false) }
    var keluhan by remember { mutableStateOf("") }

    // Dummy data
    val poliOptions = listOf(
        "Poli Umum",
        "Poli Gigi",
        "Poli Anak",
        "Poli Kandungan",
        "Poli Mata",
        "Poli THT",
        "Poli Kulit dan Kelamin",
        "Poli Penyakit Dalam",
        "Poli Bedah",
        "Poli Jantung",
        "Poli Saraf",
        "Poli Ortopedi"
    )
    val scheduleOptions = listOf(
        "07:00 - 08:00",
        "08:00 - 09:00",
        "09:00 - 10:00",
        "10:00 - 11:00",
        "11:00 - 12:00",
        "13:00 - 14:00",
        "14:00 - 15:00",
        "15:00 - 16:00",
        "16:00 - 17:00",
        "17:00 - 18:00"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Ambil Antrean",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Scrollable form content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Section 1: Peserta
            Text(
                text = "Peserta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = "Budi Santoso (0001234567890)",
                onValueChange = {},
                label = { Text("Nama Peserta (No BPJS)") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    disabledContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Faskes Tingkat Pertama
            Text(
                text = "Faskes Tingkat Pertama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dr. Eko, Sp.PD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Address",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Jl. Sudirman No. 78, Jakarta Selatan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Phone",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(021) 5559012",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Pilih Poli
            Text(
                text = "Pilih Poli",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedPoli,
                onExpandedChange = { expandedPoli = it }
            ) {
                OutlinedTextField(
                    value = selectedPoli,
                    onValueChange = {},
                    label = { Text("Pilih Poli") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPoli)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expandedPoli,
                    onDismissRequest = { expandedPoli = false }
                ) {
                    poliOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedPoli = option
                                expandedPoli = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: Pilih Tanggal Daftar
            Text(
                text = "Pilih Tanggal Daftar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Tanggal") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Pick Date"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 5: Pilih Jadwal
            Text(
                text = "Pilih Jadwal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedSchedule,
                onExpandedChange = { expandedSchedule = it }
            ) {
                OutlinedTextField(
                    value = selectedSchedule,
                    onValueChange = {},
                    label = { Text("Pilih Waktu") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSchedule)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expandedSchedule,
                    onDismissRequest = { expandedSchedule = false }
                ) {
                    scheduleOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedSchedule = option
                                expandedSchedule = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 6: Keluhan
            Text(
                text = "Keluhan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = keluhan,
                onValueChange = { keluhan = it },
                label = { Text("Tulis keluhan Anda") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 7: Simpan Button
            Button(
                onClick = {
                    // Handle save action
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Simpan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
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
