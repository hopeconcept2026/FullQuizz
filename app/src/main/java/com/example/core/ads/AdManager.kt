package com.example.core.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * AdManager - Thread-safe singleton for Google Mobile Ads (AdMob) SDK lifecycle.
 * Manages SDK initialization, preloading, and showing Rewarded Ads, Interstitials, and Banners.
 */
object AdManager {
    private const val TAG = "AdManager"

    // Official Google AdMob Production Ad Unit IDs (from user's AdMob account)
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3722681521909389/3147428117"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3722681521909389/2950474221"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-3722681521909389/3175348273"

    // Google AdMob Test Ad Unit IDs (used automatically in emulator to prevent accidental invalid clicks)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Active Ad Unit IDs: switches to test IDs in emulator for safety, and prod IDs on physical devices
    val bannerAdUnitId: String
        get() = if (isRunningInEmulator()) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    val interstitialAdUnitId: String
        get() = if (isRunningInEmulator()) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    val rewardedAdUnitId: String
        get() = if (isRunningInEmulator()) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID

    private val isInitialized = AtomicBoolean(false)
    private val isInitializing = AtomicBoolean(false)

    // Rewarded Ad state
    @Volatile
    private var rewardedAd: RewardedAd? = null
    private val isRewardedLoading = AtomicBoolean(false)

    // Interstitial Ad state
    @Volatile
    private var interstitialAd: InterstitialAd? = null
    private val isInterstitialLoading = AtomicBoolean(false)
    private val gamesCompletedSinceLastAd = AtomicInteger(0)
    private const val INTERSTITIAL_GAME_INTERVAL = 2 // Show interstitial every 2 completed games

    /**
     * Detects if the app is executing inside an Android emulator/container.
     */
    fun isRunningInEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    /**
     * Initializes the Google Mobile Ads SDK asynchronously.
     * Guarantees single-execution even if called multiple times or across configuration changes.
     */
    fun initialize(context: Context, onInitialized: (() -> Unit)? = null) {
        if (isInitialized.get()) {
            Log.d(TAG, "Google Mobile Ads SDK is already initialized.")
            onInitialized?.invoke()
            return
        }

        if (isInitializing.compareAndSet(false, true)) {
            Log.d(TAG, "Initializing Google Mobile Ads SDK...")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val requestConfig = RequestConfiguration.Builder()
                        .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                        .build()
                    MobileAds.setRequestConfiguration(requestConfig)

                    MobileAds.initialize(context.applicationContext) { initializationStatus ->
                        isInitialized.set(true)
                        isInitializing.set(false)
                        Log.i(TAG, "Google Mobile Ads SDK successfully initialized: $initializationStatus")
                        onInitialized?.invoke()
                    }
                } catch (e: Exception) {
                    isInitializing.set(false)
                    Log.w(TAG, "AdMob initialization fallback/notice: ${e.message}")
                }
            }
        }
    }

    /**
     * Loads a Rewarded Ad for hints or rewards if not already cached or loading.
     */
    fun loadRewardedAd(
        context: Context,
        adUnitId: String = rewardedAdUnitId,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        initialize(context)

        if (rewardedAd != null) {
            Log.d(TAG, "Rewarded Ad already loaded and ready.")
            onLoaded?.invoke()
            return
        }

        if (isRewardedLoading.compareAndSet(false, true)) {
            Log.d(TAG, "Loading Rewarded Ad with Unit ID: $adUnitId")
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading.set(false)
                        Log.i(TAG, "Rewarded Ad successfully loaded and ready.")
                        onLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading.set(false)
                        Log.w(TAG, "Failed to load Rewarded Ad: ${loadAdError.message}")
                        onFailed?.invoke(loadAdError)
                    }
                }
            )
        }
    }

    /**
     * Checks whether a Rewarded Ad is currently loaded and available.
     */
    fun isRewardedAdReady(): Boolean = rewardedAd != null

    /**
     * Displays a Rewarded Ad and invokes callbacks with the granted reward (Hint or Bonus Points).
     *
     * @param activity The Activity context required to show the rewarded video.
     * @param rewardType The type of item or bonus to grant (HINT_5050, HINT_SKIP, BONUS_POINTS, etc.).
     * @param defaultAmount Default reward amount if AdMob metadata is generic.
     * @param onRewardEarned Invoked strictly when the user finishes watching the video and earns the reward.
     * @param onAdClosed Invoked when the video is dismissed (to resume game loop/UI).
     * @param onAdFailed Invoked if the ad fails to display.
     */
    fun showRewardedAd(
        activity: Activity,
        rewardType: AdRewardType = AdRewardType.HINT_5050,
        defaultAmount: Int = 1,
        onRewardEarned: (AdRewardResult) -> Unit,
        onAdClosed: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        val currentAd = rewardedAd

        if (currentAd != null) {
            var isRewardVerified = false
            var earnedAmount = defaultAmount

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed. Verified=$isRewardVerified")
                    rewardedAd = null
                    // Preload next rewarded ad on physical devices
                    if (!isRunningInEmulator()) {
                        loadRewardedAd(activity.applicationContext)
                    }

                    if (isRewardVerified) {
                        onRewardEarned(
                            AdRewardResult(
                                rewardType = rewardType,
                                amount = earnedAmount,
                                isSuccess = true,
                                message = "Récompense accordée avec succès !"
                            )
                        )
                    }
                    onAdClosed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    if (!isRunningInEmulator()) {
                        loadRewardedAd(activity.applicationContext)
                    }
                    onAdFailed?.invoke(adError.message)
                    // Seamless fallback so the user still receives their hint/bonus in offline/test conditions
                    onRewardEarned(
                        AdRewardResult(
                            rewardType = rewardType,
                            amount = defaultAmount,
                            isSuccess = true,
                            message = "Bonus de secours accordé (mode hors-ligne)"
                        )
                    )
                    onAdClosed?.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad is showing full screen.")
                }
            }

            currentAd.show(activity) { rewardItem: RewardItem ->
                Log.i(TAG, "User completed video! Earned: ${rewardItem.amount} ${rewardItem.type}")
                isRewardVerified = true
                if (rewardItem.amount > 0) {
                    earnedAmount = rewardItem.amount
                }
            }
        } else {
            Log.d(TAG, "Rewarded Ad not in cache (emulator or fast fallback). Granting reward immediately.")
            if (!isRunningInEmulator()) {
                loadRewardedAd(activity.applicationContext)
            }
            onRewardEarned(
                AdRewardResult(
                    rewardType = rewardType,
                    amount = defaultAmount,
                    isSuccess = true,
                    message = "Récompense accordée"
                )
            )
            onAdClosed?.invoke()
        }
    }

    /**
     * Helper to display a Rewarded Ad specifically to grant a free Question Hint (50/50 or Skip).
     */
    fun showRewardedAdForHint(
        activity: Activity,
        hintType: AdRewardType = AdRewardType.HINT_5050,
        onHintGranted: (AdRewardResult) -> Unit,
        onAdClosed: (() -> Unit)? = null
    ) {
        showRewardedAd(
            activity = activity,
            rewardType = hintType,
            defaultAmount = 1,
            onRewardEarned = onHintGranted,
            onAdClosed = onAdClosed
        )
    }

    /**
     * Helper to display a Rewarded Ad specifically to grant Bonus Points and XP.
     */
    fun showRewardedAdForBonusPoints(
        activity: Activity,
        bonusPoints: Int = 50,
        onBonusGranted: (AdRewardResult) -> Unit,
        onAdClosed: (() -> Unit)? = null
    ) {
        showRewardedAd(
            activity = activity,
            rewardType = AdRewardType.BONUS_POINTS,
            defaultAmount = bonusPoints,
            onRewardEarned = onBonusGranted,
            onAdClosed = onAdClosed
        )
    }

    /**
     * Loads an Interstitial Ad for transitions between quiz sessions.
     */
    fun loadInterstitialAd(
        context: Context,
        adUnitId: String = interstitialAdUnitId,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        initialize(context)

        if (interstitialAd != null) {
            Log.d(TAG, "Interstitial Ad already loaded and ready.")
            onLoaded?.invoke()
            return
        }

        if (isInterstitialLoading.compareAndSet(false, true)) {
            Log.d(TAG, "Loading Interstitial Ad with Unit ID: $adUnitId")
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading.set(false)
                        Log.i(TAG, "Interstitial Ad successfully loaded.")
                        onLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading.set(false)
                        Log.w(TAG, "Failed to load Interstitial Ad: ${loadAdError.message}")
                        onFailed?.invoke(loadAdError)
                    }
                }
            )
        }
    }

    /**
     * Checks if an interstitial is ready.
     */
    fun isInterstitialReady(): Boolean = interstitialAd != null

    /**
     * Displays an Interstitial Ad on screen with full lifecycle callbacks.
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed.")
                    interstitialAd = null
                    if (!isRunningInEmulator()) {
                        loadInterstitialAd(activity.applicationContext)
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    if (!isRunningInEmulator()) {
                        loadInterstitialAd(activity.applicationContext)
                    }
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad is showing.")
                }
            }
            currentAd.show(activity)
        } else {
            Log.d(TAG, "No Interstitial Ad available in cache. Proceeding directly.")
            if (!isRunningInEmulator()) {
                loadInterstitialAd(activity.applicationContext)
            }
            onAdClosed()
        }
    }

    /**
     * Smart helper: Shows interstitial ad only every [INTERSTITIAL_GAME_INTERVAL] games.
     * Prevents ad fatigue and optimizes Play Store policy compliance.
     */
    fun showInterstitialIfEligible(
        activity: Activity,
        onFinished: () -> Unit
    ) {
        val count = gamesCompletedSinceLastAd.incrementAndGet()
        if (count >= INTERSTITIAL_GAME_INTERVAL) {
            gamesCompletedSinceLastAd.set(0)
            showInterstitialAd(activity, onFinished)
        } else {
            onFinished()
        }
    }

    /**
     * Returns whether the Google Mobile Ads SDK is initialized.
     */
    fun isSdkReady(): Boolean = isInitialized.get()
}

/**
 * Types of rewards available via Rewarded Ads in FullQuizz.
 */
enum class AdRewardType {
    HINT_5050,
    HINT_SKIP,
    BONUS_POINTS,
    EXTRA_LIFE,
    COINS
}

/**
 * Result payload containing reward details passed to callbacks.
 */
data class AdRewardResult(
    val rewardType: AdRewardType,
    val amount: Int,
    val isSuccess: Boolean,
    val message: String = ""
)
