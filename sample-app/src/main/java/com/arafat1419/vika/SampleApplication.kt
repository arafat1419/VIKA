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
                    description = "Main home screen with featured products and categories",
                    deepLink = "sample://home",
                    navigationType = NavigationType.DeepLink("sample://home"),
                    keywords = listOf("home", "main", "start", "beginning", "dashboard")
                ),

                ScreenRegistration(
                    screenId = "product_list",
                    screenName = "Product List",
                    description = "Browse all available products with filters and search",
                    deepLink = "sample://products",
                    navigationType = NavigationType.DeepLink("sample://products"),
                    keywords = listOf(
                        "products",
                        "browse",
                        "shop",
                        "catalog",
                        "items",
                        "all products"
                    )
                ),

                ScreenRegistration(
                    screenId = "product_detail",
                    screenName = "Product Detail",
                    description = "Shows detailed information about a specific product including price, description, reviews, and images",
                    deepLink = "sample://product",
                    navigationType = NavigationType.DeepLink("sample://product"),
                    keywords = listOf(
                        "product",
                        "item",
                        "details",
                        "info",
                        "specifications",
                        "single product"
                    )
                ),

                ScreenRegistration(
                    screenId = "cart",
                    screenName = "Shopping Cart",
                    description = "View and manage items in shopping cart, update quantities, and proceed to checkout",
                    deepLink = "sample://cart",
                    navigationType = NavigationType.DeepLink("sample://cart"),
                    keywords = listOf(
                        "cart",
                        "basket",
                        "bag",
                        "checkout",
                        "purchase",
                        "buy",
                        "shopping cart"
                    )
                ),

                ScreenRegistration(
                    screenId = "profile",
                    screenName = "User Profile",
                    description = "View and edit user profile information, settings, order history, and preferences",
                    deepLink = "sample://profile",
                    navigationType = NavigationType.DeepLink("sample://profile"),
                    keywords = listOf(
                        "profile",
                        "account",
                        "settings",
                        "user",
                        "my account",
                        "personal"
                    )
                )
            )
        )
    }
}