package com.notix.notixsdk.interstitial

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.commit451.coiltransformations.BlurTransformation
import com.notix.notixsdk.R
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class InterstitialActivity : AppCompatActivity() {

    companion object {
        const val DATA = "INTERSTITIAL_DATA"
    }

    private val image: ImageView by lazy { findViewById(R.id.interstitial_image) }
    private val background: ImageView by lazy { findViewById(R.id.interstitial_background) }
    private val titleText: TextView by lazy { findViewById(R.id.interstitial_title) }
    private val descriptionText: TextView by lazy { findViewById(R.id.interstitial_description) }
    private val button: TextView by lazy { findViewById(R.id.interstitial_button) }
    private val closeButton: View by lazy { findViewById(R.id.interstitial_close) }
    private val progressView: View by lazy { findViewById(R.id.interstitial_progress) }

    private val apiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        prepareActivity()
        setContentView(R.layout.activity_interstitial)

        val data = intent.getSerializableExtra(DATA) as? InterstitialData
        if (data == null) {
            closeInterstitial(InterstitialContract.RESULT_ERROR)
            return
        }
        lifecycleScope.safeLaunch {
            composeInterstitial(data)
        }
    }

    private fun prepareActivity() {
        this.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            observeInsets(window.decorView)
        }
    }

    private fun observeInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val topInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val rightInset = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).right
            applyInsets(topInset, rightInset)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun applyInsets(topInset: Int, rightInset: Int) {
        val layoutParams = (closeButton.layoutParams as? ConstraintLayout.LayoutParams)
            ?.apply {
                topMargin = topInset
                rightMargin = (8f.dp + rightInset).toInt()
            }
        closeButton.layoutParams = layoutParams
    }

    private suspend fun composeInterstitial(data: InterstitialData) {
        val drawable = loadImage(data.imageUrl)
        progressView.isVisible = false
        image.setImageDrawable(drawable)
        setBlurredBackground(drawable)
        configureControls(data, drawable)
        apiClient.impression(this, data.impressionData)
    }

    private suspend fun loadImage(url: String): Drawable? = suspendCoroutine { continuation ->
        ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false)
            .target(
                onSuccess = { continuation.resume(it) },
                onError = {
                    continuation.resume(it)
                    closeInterstitial(InterstitialContract.RESULT_ERROR)
                }
            )
            .build()
            .also(imageLoader::enqueue)
    }

    private fun setBlurredBackground(image: Drawable?) {
        if (image == null) return
        background.load(image) {
            transformations(BlurTransformation(this@InterstitialActivity))
        }
    }

    private suspend fun configureControls(data: InterstitialData, drawable: Drawable?) {
        val colors = ColorPairsGenerator.generate(this, drawable)
        // use primary color for button if it exists for text, otherwise
        val textColors = if (data.buttons.isNotEmpty()) {
            colors.getOrNull(1) ?: colors.first()
        } else {
            colors.first()
        }
        val buttonColors = colors.first()
        drawTexts(data, textColors)
        drawButtons(data, buttonColors)
        configureCloseButton(data)
        setCloseListener()
        setClickListener(data)
    }

    private fun drawTexts(data: InterstitialData, colorsPair: Pair<Int, Int>) {
        val textColor = colorsPair.first
        val textBackground = generateBackgroundDrawable(colorsPair.second, 4.dp)
        titleText.background = textBackground
        descriptionText.background = textBackground
        titleText.setTextColor(textColor)
        descriptionText.setTextColor(textColor)
        titleText.text = data.title
        descriptionText.text = data.description
    }

    private fun drawButtons(data: InterstitialData, colorsPair: Pair<Int, Int>) {
        val buttonSpec = data.buttons.firstOrNull() ?: return
        val buttonColor = colorsPair.first
        val buttonBackground = generateBackgroundDrawable(colorsPair.second, 16.dp)
        with(button) {
            visibility = View.VISIBLE
            text = buttonSpec.text
            background = buttonBackground
            setTextColor(buttonColor)
        }
    }

    private fun generateBackgroundDrawable(@ColorInt color: Int, radius: Float) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }

    private fun configureCloseButton(data: InterstitialData) {
        val closingSettings = data.closingSettings
        val showButtonTime = System.currentTimeMillis() + closingSettings.timeout * 1000
        lifecycleScope.safeLaunch {
            val buttonWR = WeakReference(closeButton)
            while (isActive) {
                if (System.currentTimeMillis() >= showButtonTime) {
                    buttonWR.get()?.visibility = View.VISIBLE
                    break
                }
                delay(1000)
            }
        }

        if (closingSettings.opacity in 0f..1f) {
            closeButton.alpha = closingSettings.opacity
        }

        val sizeMultiplier = closingSettings.sizePercent
        if (sizeMultiplier in 0f..2f) {
            val layoutParams = (closeButton.layoutParams as? ConstraintLayout.LayoutParams)
                ?.apply {
                    val size = (closeButton.layoutParams.width * sizeMultiplier).toInt()
                    width = size
                    height = size
                }
            closeButton.layoutParams = layoutParams
        }
    }

    private fun setCloseListener() {
        closeButton.setOnClickListener {
            closeInterstitial(InterstitialContract.RESULT_DISMISSED)
        }
    }

    private fun setClickListener(data: InterstitialData) {
        val clickListener = composeClickListener(data.targetUrl, data.openExternalBrowser)
        image.setOnClickListener(clickListener)
        background.setOnClickListener(clickListener)
        titleText.setOnClickListener(clickListener)
        descriptionText.setOnClickListener(clickListener)

        if (data.buttons.isNotEmpty()) {
            val btnClickListener = data.buttons.first().targetUrl?.let { btnTargetUrl ->
                composeClickListener(btnTargetUrl, data.openExternalBrowser)
            } ?: clickListener
            button.setOnClickListener(btnClickListener)
        }
    }

    private fun composeClickListener(url: String, isExternal: Boolean) = View.OnClickListener {
        navigateToUrl(url, isExternal)
        closeInterstitial(InterstitialContract.RESULT_CLICKED)
    }

    private fun navigateToUrl(url: String, isExternal: Boolean) {
        when {
            url.isBlank() -> Log.d("NotixDebug", "Target url is empty")
            !url.contains("http") -> Log.d("NotixDebug", "Target url must contains http")
            else -> {
                val uri = Uri.parse(url)
                if (isExternal) {
                    openBrowser(uri)
                } else {
                    openCustomTabsBrowser(uri)
                }
            }
        }
    }

    private fun openBrowser(uri: Uri) {
        if (canHandleBrowserIntent()) {
            Intent(Intent.ACTION_VIEW, uri)
                .also { intent -> startActivity(intent) }
        } else {
            Log.d("NotixDebug", "Android device does not support Web browsing")
        }
    }

    private fun openCustomTabsBrowser(uri: Uri) {
        if (hasCustomTabsBrowser()) {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(this, uri)
        } else {
            Log.d("NotixDebug", "Android device does not support Web browsing")
        }
    }

    private fun closeInterstitial(result: Int) {
        setResult(result)
        finish()
    }

    override fun onBackPressed() = Unit
}