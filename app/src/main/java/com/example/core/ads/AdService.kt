package com.example.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.core.analytics.AnalyticsService
import com.example.core.constants.QuizConstants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RewardedType {
    EXTRA_LIFE,
    EXTRA_COINS,
    FREE_HINT,
    DOUBLE_REWARD
}

/**
 * AdService - Google Mobile Ads (AdMob) SDK Manager.
 * Handles non-intrusive Banners, Interstitials (paced), and Rewarded Ads for bonus lives/hints/XP.
 * Includes graceful offline fallbacks and Google standard sample test IDs.
 */
class AdService(private val context: Context) {

    companion object {
        private const val TAG = "FULLQUIZZ_AdService"

        // Google AdMob Test Ad Unit IDs (replace with production IDs before Play Store release)
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var lastInterstitialTime = 0L
    private var quizzesSinceLastInterstitial = 0

    init {
        // Ads are loaded on-demand to optimize memory and battery usage
    }

    fun preloadAds() {
        if (!AdManager.isRunningInEmulator()) {
            preloadInterstitial()
            preloadRewarded()
        }
    }

    private fun preloadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial Ad loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.d(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    private fun preloadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded Ad loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    Log.d(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun shouldShowInterstitial(): Boolean {
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - lastInterstitialTime) / 1000
        return elapsedSeconds >= QuizConstants.MIN_SECONDS_BETWEEN_INTERSTITIALS &&
                quizzesSinceLastInterstitial >= QuizConstants.MIN_QUIZZES_BETWEEN_INTERSTITIALS
    }

    fun onQuizCompleted(activity: Activity? = null, onAdDismissed: () -> Unit) {
        quizzesSinceLastInterstitial++
        if (shouldShowInterstitial()) {
            lastInterstitialTime = System.currentTimeMillis()
            quizzesSinceLastInterstitial = 0
            AnalyticsService.logEvent("interstitial_ad_shown")

            val ad = interstitialAd
            if (ad != null && activity != null) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        preloadInterstitial()
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        preloadInterstitial()
                        onAdDismissed()
                    }
                }
                ad.show(activity)
            } else {
                preloadInterstitial()
                onAdDismissed()
            }
        } else {
            onAdDismissed()
        }
    }

    fun showRewardedAd(
        activity: Activity? = null,
        type: RewardedType,
        onRewardGranted: () -> Unit,
        onAdFailed: (String) -> Unit = {}
    ) {
        AnalyticsService.logAdWatched("rewarded", type.name)
        val ad = rewardedAd
        if (ad != null && activity != null) {
            _isAdLoading.value = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    _isAdLoading.value = false
                    rewardedAd = null
                    preloadRewarded()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    _isAdLoading.value = false
                    rewardedAd = null
                    preloadRewarded()
                    // Fallback to giving the reward in case of network issue
                    onRewardGranted()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardGranted()
            }
        } else {
            // Immediate simulated reward for offline or emulator testing
            preloadRewarded()
            onRewardGranted()
        }
    }
}
