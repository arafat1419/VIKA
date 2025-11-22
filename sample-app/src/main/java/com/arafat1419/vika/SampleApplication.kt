package com.arafat1419.vika

import android.app.Application
import com.vika.sdk.BuildConfig
import com.vika.sdk.VikaSDK
import com.vika.sdk.models.NavigationType
import com.vika.sdk.models.SDKConfig
import com.vika.sdk.models.ScreenRegistration
import com.vika.sdk.models.VikaLanguage

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = SDKConfig.Builder("J6pruwy4Pz8GjJ8pAKsa7a8U3wQ3Z1X7")
            .minConfidenceThreshold(0.75f)
            .analyticsEnabled(true)
            .debugMode(BuildConfig.DEBUG)
            .allowedDeepLinkSchemes("sample")
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
                    screenId = "home",
                    screenName = "Home",
                    description = "Halaman utama dengan antrean online, menu layanan, dan informasi JKN",
                    deepLink = "sample://home",
                    navigationType = NavigationType.DeepLink("sample://home"),
                    keywords = listOf("home", "beranda", "utama", "main", "dashboard")
                ),

                ScreenRegistration(
                    screenId = "berita",
                    screenName = "Berita",
                    description = "Informasi dan berita terkini seputar JKN dan layanan kesehatan",
                    deepLink = "sample://berita",
                    navigationType = NavigationType.DeepLink("sample://berita"),
                    keywords = listOf("berita", "news", "informasi", "artikel", "pengumuman")
                ),

                ScreenRegistration(
                    screenId = "kartu",
                    screenName = "Kartu",
                    description = "Informasi kartu JKN, nomor peserta, dan status kepesertaan",
                    deepLink = "sample://kartu",
                    navigationType = NavigationType.DeepLink("sample://kartu"),
                    keywords = listOf("kartu", "card", "jkn card", "peserta", "nomor kartu")
                ),

                ScreenRegistration(
                    screenId = "faq",
                    screenName = "FAQ",
                    description = "Pertanyaan yang sering diajukan seputar layanan JKN",
                    deepLink = "sample://faq",
                    navigationType = NavigationType.DeepLink("sample://faq"),
                    keywords = listOf("faq", "pertanyaan", "help", "bantuan", "tanya jawab")
                ),

                ScreenRegistration(
                    screenId = "profile",
                    screenName = "Profile",
                    description = "Informasi profil pengguna, pengaturan akun, dan data pribadi",
                    deepLink = "sample://profile",
                    navigationType = NavigationType.DeepLink("sample://profile"),
                    keywords = listOf(
                        "profile",
                        "profil",
                        "account",
                        "akun",
                        "pengaturan",
                        "settings"
                    )
                )
            )
        )
    }
}