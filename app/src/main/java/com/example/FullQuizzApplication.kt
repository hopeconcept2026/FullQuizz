package com.example

import android.app.Application
import android.util.Log
import com.example.core.ads.AdManager

/**
 * FullQuizzApplication - Main Application class for FullQuizz.
 * Initializes Google Mobile Ads (AdMob) and core services on application startup.
 */
class FullQuizzApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "FullQuizzApplication onCreate - Initializing MobileAds SDK...")

        // Initialize Google Mobile Ads SDK as early as possible in Application onCreate
        AdManager.initialize(this) {
            Log.i(TAG, "MobileAds initialized successfully in Application context.")
            if (!AdManager.isRunningInEmulator()) {
                AdManager.loadRewardedAd(this)
                AdManager.loadInterstitialAd(this)
            }
        }
    }

    companion object {
        private const val TAG = "FullQuizzApplication"
    }
}
