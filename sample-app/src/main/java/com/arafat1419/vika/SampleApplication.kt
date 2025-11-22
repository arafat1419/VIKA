package com.arafat1419.vika

import android.app.Application
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.NavigationType
import com.vika.sdk.models.SDKConfig
import com.vika.sdk.models.ScreenRegistration
import com.vika.sdk.models.VikaLanguage

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = SDKConfig.Builder(BuildConfig.VIKA_API_KEY)
            .minConfidenceThreshold(0.75f)
            .analyticsEnabled(true)
            .debugMode(BuildConfig.DEBUG)
            .allowedDeepLinkSchemes("bpjskes")
            .language(VikaLanguage.INDONESIAN)
            .build()

        VikaSDK.initialize(
            context = this,
            config = config,
            callback = object : VikaSDK.Companion.InitCallback {
                override fun onSuccess() {
                    // Register screens after SDK is initialized with backend
                    registerScreens()
                }

                override fun onError(error: Throwable) {
                    // Still register screens locally for offline use
                    registerScreens()
                }
            }
        )
    }

    private fun registerScreens() {
        val sdk = VikaSDK.getInstance()

        sdk.registerScreens(
            listOf(
                ScreenRegistration(
                    screenId = "info_program_jkn",
                    screenName = "Info Program JKN",
                    description = "Menu ini berisi rangkuman menyeluruh mengenai Program Jaminan Kesehatan Nasional (JKN), termasuk tujuan penyelenggaraan, konsep gotong royong, kelompok sasaran peserta, manfaat yang dijamin, serta cara mengakses pelayanan di fasilitas kesehatan. Informasi disajikan dengan struktur yang mudah dipahami sehingga membantu peserta baru maupun lama mengenal cara kerja program serta hak dan kewajiban mereka dalam kepesertaan JKN.",
                    deepLink = "bpjskes://info_program_jkn",
                    navigationType = NavigationType.DeepLink("bpjskes://info_program_jkn"),
                    keywords = listOf("info", "umum")
                ),
                ScreenRegistration(
                    screenId = "telehealth",
                    screenName = "Telehealth",
                    description = "Telehealth memberikan akses konsultasi kesehatan jarak jauh dengan tenaga medis yang terlatih. Layanan ini memungkinkan peserta mendapatkan penilaian awal, arahan perawatan mandiri, penjelasan obat, hingga rekomendasi untuk datang ke fasilitas kesehatan bila diperlukan. Fitur ini dirancang untuk memberikan solusi cepat, efisien, dan aman terutama pada kondisi non-gawat darurat.",
                    deepLink = "bpjskes://telehealth",
                    navigationType = NavigationType.DeepLink("bpjskes://telehealth"),
                    keywords = listOf("telehealth", "konsultasi", "dokter", "online", "kesehatan")
                ),
                ScreenRegistration(
                    screenId = "info_riwayat_pelayanan",
                    screenName = "Info Riwayat Pelayanan",
                    description = "Menu ini menyajikan daftar lengkap pelayanan kesehatan yang pernah diterima peserta, meliputi kunjungan ke faskes tingkat pertama, rujukan, tindakan medis, hingga obat yang diberikan. Fitur ini memudahkan peserta mengevaluasi kondisi kesehatan pribadi, melihat pola kunjungan, serta membantu tenaga medis dalam mengkaji riwayat kesehatan saat diperlukan.",
                    deepLink = "bpjskes://info_riwayat_pelayanan",
                    navigationType = NavigationType.DeepLink("bpjskes://info_riwayat_pelayanan"),
                    keywords = listOf("riwayat", "pelayanan", "faskes", "kunjungan", "medis")
                ),
                ScreenRegistration(
                    screenId = "bugar",
                    screenName = "Bugar",
                    description = "Menu Bugar menyediakan konten edukatif mengenai gaya hidup sehat, termasuk rekomendasi aktivitas fisik, pola makan, tips kesehatan harian, serta panduan menjaga kebugaran tubuh. Informasi disajikan oleh tenaga kesehatan dan disesuaikan dengan kebutuhan masyarakat sehingga peserta dapat membentuk kebiasaan hidup sehat secara berkelanjutan.",
                    deepLink = "bpjskes://bugar",
                    navigationType = NavigationType.DeepLink("bpjskes://bugar"),
                    keywords = listOf("sehat", "bugar", "gaya", "hidup", "edukasi")
                ),
                ScreenRegistration(
                    screenId = "new_rehap",
                    screenName = "Cicilan REHAP",
                    description = "Fitur ini memungkinkan peserta mensimulasikan dan mengatur pembayaran tunggakan iuran melalui skema REHAP. Peserta dapat melihat estimasi cicilan, pilihan jangka waktu, syarat program, hingga status pembayaran berjalan. Menu ini memudahkan peserta mengelola kewajiban iuran secara lebih ringan dan terencana.",
                    deepLink = "bpjskes://new_rehap",
                    navigationType = NavigationType.DeepLink("bpjskes://new_rehap"),
                    keywords = listOf("rehap", "cicilan", "tunggakan", "iuran", "pembayaran")
                ),
                ScreenRegistration(
                    screenId = "penambahan_peserta",
                    screenName = "Penambahan Peserta",
                    description = "Menu ini digunakan untuk menambahkan anggota keluarga ke dalam kepesertaan JKN. Peserta dapat melihat syarat dokumen, proses validasi, kategori hubungan keluarga yang diizinkan, serta prosedur verifikasi data. Fitur ini membantu peserta memperbarui data kepesertaan tanpa harus datang ke kantor BPJS.",
                    deepLink = "bpjskes://penambahan_peserta",
                    navigationType = NavigationType.DeepLink("bpjskes://penambahan_peserta"),
                    keywords = listOf("penambahan", "peserta", "keluarga", "pendaftaran", "data")
                ),
                ScreenRegistration(
                    screenId = "info_peserta",
                    screenName = "Info Peserta",
                    description = "Menu ini memuat informasi lengkap mengenai status kepesertaan seperti segmen peserta, faskes terdaftar, status aktif/nonaktif, nomor kartu, serta anggota keluarga yang terdaftar. Peserta dapat memastikan keakuratan data mereka dan melakukan penyesuaian bila diperlukan.",
                    deepLink = "bpjskes://info_peserta",
                    navigationType = NavigationType.DeepLink("bpjskes://info_peserta"),
                    keywords = listOf("peserta", "informasi", "kepesertaan", "data", "profil")
                ),
                ScreenRegistration(
                    screenId = "sos",
                    screenName = "SOS",
                    description = "Menu SOS menyediakan akses cepat menuju informasi dan panduan tindakan awal ketika menghadapi kondisi darurat. Fitur ini dapat mencakup nomor kontak penting, lokasi fasilitas kesehatan terdekat, serta petunjuk langkah segera yang aman sebelum mendapatkan pertolongan medis profesional.",
                    deepLink = "bpjskes://sos",
                    navigationType = NavigationType.DeepLink("bpjskes://sos"),
                    keywords = listOf("sos", "darurat", "bantuan", "gawat", "kontak")
                ),
                ScreenRegistration(
                    screenId = "info_lokasi_faskes",
                    screenName = "Info Lokasi Faskes",
                    description = "Fitur ini menampilkan daftar lengkap fasilitas kesehatan (FKTP dan FKRTL) yang bekerja sama dengan BPJS Kesehatan. Peserta dapat mencari lokasi berdasarkan alamat, kota, jenis layanan, serta melihat detail seperti jam operasional, nomor kontak, dan ketersediaan layanan tertentu.",
                    deepLink = "bpjskes://info_lokasi_faskes",
                    navigationType = NavigationType.DeepLink("bpjskes://info_lokasi_faskes"),
                    keywords = listOf("faskes", "lokasi", "peta", "pelayanan", "kesehatan")
                ),
                ScreenRegistration(
                    screenId = "perubahan_data_peserta",
                    screenName = "Perubahan Data Peserta",
                    description = "Menu ini digunakan untuk memperbarui informasi kepesertaan seperti alamat, identitas, nomor kontak, faskes pilihan, atau perubahan status keanggotaan. Fitur ini membantu peserta menjaga agar data mereka tetap valid sehingga pelayanan kesehatan dapat diakses tanpa hambatan administratif.",
                    deepLink = "bpjskes://perubahan_data_peserta",
                    navigationType = NavigationType.DeepLink("bpjskes://perubahan_data_peserta"),
                    keywords = listOf("perubahan", "data", "peserta", "update", "profil")
                ),
                ScreenRegistration(
                    screenId = "pengaduan_layanan_jkn",
                    screenName = "Pengaduan Layanan JKN",
                    description = "Fitur ini memberikan sarana bagi peserta untuk menyampaikan keluhan terkait pelayanan kesehatan, proses administrasi, akses fasilitas kesehatan, atau masalah kepesertaan lainnya. Menu menyediakan alur pengaduan yang jelas, form pengisian, serta mekanisme tindak lanjut yang transparan.",
                    deepLink = "bpjskes://pengaduan_layanan_jkn",
                    navigationType = NavigationType.DeepLink("bpjskes://pengaduan_layanan_jkn"),
                    keywords = listOf("pengaduan", "keluhan", "layanan", "jkn", "aduan")
                ),
                ScreenRegistration(
                    screenId = "skrining_riwayat_kesehatan",
                    screenName = "Skrining Riwayat Kesehatan",
                    description = "Menu ini memungkinkan peserta melakukan skrining mandiri untuk mendeteksi potensi risiko penyakit kronis sejak dini. Peserta akan mengisi pertanyaan terkait gaya hidup, kondisi kesehatan, dan riwayat medis. Di akhir, sistem memberikan hasil analisis risiko serta rekomendasi tindak lanjut.",
                    deepLink = "bpjskes://skrining_riwayat_kesehatan",
                    navigationType = NavigationType.DeepLink("bpjskes://skrining_riwayat_kesehatan"),
                    keywords = listOf("skrining", "kesehatan", "risiko", "mandiri", "medis")
                ),
                ScreenRegistration(
                    screenId = "pendaftaran_pelayanan",
                    screenName = "Pendaftaran Pelayanan",
                    description = "Fitur antrean online memungkinkan peserta mendaftar layanan di fasilitas kesehatan tanpa harus datang lebih awal. Peserta dapat memilih faskes, poli tujuan, jadwal kedatangan, serta menerima nomor antrean digital. Layanan ini membantu mengurangi waktu tunggu dan memperbaiki pengalaman pelayanan.",
                    deepLink = "bpjskes://pendaftaran_pelayanan",
                    navigationType = NavigationType.DeepLink("bpjskes://pendaftaran_pelayanan"),
                    keywords = listOf("antrean", "pendaftaran", "online", "pelayanan", "faskes")
                ),
                ScreenRegistration(
                    screenId = "info_ketersediaan_tempat_tidur",
                    screenName = "Info Ketersediaan Tempat Tidur",
                    description = "Fitur ini menampilkan informasi terbaru mengenai jumlah tempat tidur kosong di fasilitas kesehatan rujukan. Data ditampilkan berdasarkan kelas perawatan, jenis layanan, dan status ketersediaan. Menu ini sangat membantu bagi peserta yang membutuhkan perawatan inap secara cepat dan terarah.",
                    deepLink = "bpjskes://info_ketersediaan_tempat_tidur",
                    navigationType = NavigationType.DeepLink("bpjskes://info_ketersediaan_tempat_tidur"),
                    keywords = listOf("tempat", "tidur", "tersedia", "rujukan", "inap")
                ),
                ScreenRegistration(
                    screenId = "info_jadwal_tindakan_operasi",
                    screenName = "Info Jadwal Tindakan Operasi",
                    description = "Menu ini memberikan informasi jadwal tindakan operasi yang telah terdaftar di fasilitas kesehatan. Peserta dapat melihat daftar antrean, estimasi waktu pelaksanaan, persiapan pra-operasi, serta catatan penting dari rumah sakit. Informasi ini membantu peserta mengatur waktu dan mempersiapkan kebutuhan perawatan.",
                    deepLink = "bpjskes://info_jadwal_tindakan_operasi",
                    navigationType = NavigationType.DeepLink("bpjskes://info_jadwal_tindakan_operasi"),
                    keywords = listOf("operasi", "jadwal", "tindakan", "medis", "rujukan")
                ),
                ScreenRegistration(
                    screenId = "info_iuran",
                    screenName = "Info Iuran",
                    description = "Fitur ini menyediakan rangkuman lengkap mengenai besaran iuran untuk tiap segmen peserta, ketentuan pembayaran, denda, serta skema subsidi pemerintah. Peserta juga dapat mempelajari cara pembayaran yang tersedia dan batas waktu pembayaran tiap bulan.",
                    deepLink = "bpjskes://info_iuran",
                    navigationType = NavigationType.DeepLink("bpjskes://info_iuran"),
                    keywords = listOf("iuran", "pembayaran", "tarif", "kepesertaan", "biaya")
                ),
                ScreenRegistration(
                    screenId = "pendaftaran_auto_debit",
                    screenName = "Pendaftaran Auto Debit",
                    description = "Menu ini memudahkan peserta mengaktifkan fitur auto debit agar pembayaran iuran dilakukan otomatis setiap bulan melalui bank atau layanan pembayaran tertentu. Fitur ini membantu menghindari keterlambatan pembayaran dan memastikan kepesertaan tetap aktif.",
                    deepLink = "bpjskes://pendaftaran_auto_debit",
                    navigationType = NavigationType.DeepLink("bpjskes://pendaftaran_auto_debit"),
                    keywords = listOf("auto", "debit", "pembayaran", "iuran", "otomatis")
                ),
                ScreenRegistration(
                    screenId = "info_riwayat_pembayaran",
                    screenName = "Info Riwayat Pembayaran",
                    description = "Menu ini menampilkan catatan seluruh pembayaran iuran yang telah dilakukan peserta, termasuk tanggal pembayaran, metode, periode iuran, dan status transaksi. Fitur ini membantu peserta memastikan kewajiban iuran telah dipenuhi dengan benar.",
                    deepLink = "bpjskes://info_riwayat_pembayaran",
                    navigationType = NavigationType.DeepLink("bpjskes://info_riwayat_pembayaran"),
                    keywords = listOf("riwayat", "pembayaran", "iuran", "transaksi", "catatan")
                ),
                ScreenRegistration(
                    screenId = "info_virtual_account",
                    screenName = "Info Virtual Account",
                    description = "Fitur ini menyediakan informasi nomor virtual account peserta yang digunakan untuk membayar iuran melalui bank atau layanan pembayaran digital. Menu juga mencakup panduan pembayaran dan pengecekan status VA.",
                    deepLink = "bpjskes://info_virtual_account",
                    navigationType = NavigationType.DeepLink("bpjskes://info_virtual_account"),
                    keywords = listOf("virtual", "account", "pembayaran", "iuran", "va")
                ),
                ScreenRegistration(
                    screenId = "minum_obat",
                    screenName = "Minum Obat",
                    description = "Menu Minum Obat membantu peserta mengatur jadwal konsumsi obat melalui pengingat otomatis. Peserta dapat mencatat jenis obat, dosis, frekuensi konsumsi, serta menerima notifikasi untuk memastikan obat diminum sesuai anjuran dokter.",
                    deepLink = "bpjskes://minum_obat",
                    navigationType = NavigationType.DeepLink("bpjskes://minum_obat"),
                    keywords = listOf("obat", "pengingat", "kesehatan", "dosis", "konsumsi")
                ),
                ScreenRegistration(
                    screenId = "tren_penyakit_daerah",
                    screenName = "Tren Penyakit Daerah",
                    description = "Fitur ini menyajikan data statistik mengenai tren penyakit di wilayah tertentu. Informasi diambil dari laporan fasilitas kesehatan dan ditampilkan dalam bentuk grafik atau ringkasan. Peserta dapat memahami pola penyakit untuk meningkatkan kewaspadaan dan pencegahan.",
                    deepLink = "bpjskes://tren_penyakit_daerah",
                    navigationType = NavigationType.DeepLink("bpjskes://tren_penyakit_daerah"),
                    keywords = listOf("penyakit", "tren", "daerah", "statistik", "kesehatan")
                ),
                ScreenRegistration(
                    screenId = "antrean_online",
                    screenName = "Antrean Online",
                    description = "Menu ini menyediakan fitur antrean digital pada fasilitas kesehatan yang bekerja sama. Peserta dapat memilih poli, waktu kedatangan, serta memantau posisi antrean secara real-time sehingga proses pelayanan menjadi lebih efisien dan nyaman.",
                    deepLink = "bpjskes://antrean_online",
                    navigationType = NavigationType.DeepLink("bpjskes://antrean_online"),
                    keywords = listOf("antrean", "online", "faskes", "pelayanan", "jadwal")
                )
            )
        )
    }
}