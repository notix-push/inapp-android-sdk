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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.commit451.coiltransformations.BlurTransformation
import com.notix.notixsdk.R
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.utils.canHandleBrowserIntent
import com.notix.notixsdk.utils.dp
import com.notix.notixsdk.utils.hasCustomTabsBrowser

internal class InterstitialActivity : AppCompatActivity() {

    companion object {
        const val DATA = "INTERSTITIAL_DATA"
    }

    private val image: ImageView by lazy { findViewById(R.id.interstitial_image) }
    private val background: ImageView by lazy { findViewById(R.id.interstitial_background) }
    private val titleText: TextView by lazy { findViewById(R.id.interstitial_title) }
    private val descriptionText: TextView by lazy { findViewById(R.id.interstitial_description) }
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
        composeInterstitial(data)
        setCloseListener()
        setClickListener(data)
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

    private fun composeInterstitial(data: InterstitialData) {
        loadImage(data.imageUrl) { drawable ->
            progressView.isVisible = false
            closeButton.isVisible = true
            image.setImageDrawable(drawable)
            generateColors(drawable) { textColor, background ->
                drawTexts(data, textColor, background)
            }
            apiClient.impression(this, data.impressionData)
        }
        setBlurredBackground(data.imageUrl)
    }

    private fun drawTexts(data: InterstitialData, textColor: Int, background: Drawable) {
        titleText.background = background
        descriptionText.background = background
        titleText.setTextColor(textColor)
        descriptionText.setTextColor(textColor)
        setTexts(data)
    }

    private fun loadImage(url: String, callback: (Drawable?) -> Unit) {
        ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false)
            .target(
                onSuccess = callback,
                onError = { closeInterstitial(InterstitialContract.RESULT_ERROR) }
            )
            .build()
            .also(imageLoader::enqueue)
    }

    private fun setBlurredBackground(url: String) {
        background.load(url) {
            transformations(BlurTransformation(this@InterstitialActivity))
        }
    }

    private fun generateColors(
        drawable: Drawable?,
        callback: (textColor: Int, background: Drawable) -> Unit
    ) {
        if (drawable == null) {
            val textBackgroundColor = ContextCompat.getColor(this, R.color.notix_interstitial_text_background_color)
            val textColor = ContextCompat.getColor(this, R.color.notix_interstitial_text_color)
            val textBackground = generateTextBackground(textBackgroundColor)
            callback(textColor, textBackground)
        } else {
            val bitmap = drawable.toBitmap()
            Palette.from(bitmap).generate { palette ->
                val swatch = palette?.vibrantSwatch ?: palette?.lightVibrantSwatch ?: palette?.darkVibrantSwatch

                val textBackgroundColor = swatch?.rgb
                    ?: swatch?.rgb
                    ?: ContextCompat.getColor(this, R.color.notix_interstitial_text_background_color)

                val textColor = swatch?.bodyTextColor
                    ?: ContextCompat.getColor(this, R.color.notix_interstitial_text_color)

                val textBackground = generateTextBackground(textBackgroundColor)
                callback(textColor, textBackground)
            }
        }
    }

    private fun generateTextBackground(@ColorInt color: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = 4.dp
        }

    private fun setTexts(data: InterstitialData) {
        titleText.text = data.title
        descriptionText.text = data.description
    }

    private fun setCloseListener() {
        closeButton.setOnClickListener {
            closeInterstitial(InterstitialContract.RESULT_DISMISSED)
        }
    }

    private fun setClickListener(data: InterstitialData) {
        val clickListener = View.OnClickListener {
            navigateToUrl(data)
            closeInterstitial(InterstitialContract.RESULT_CLICKED)
        }
        image.setOnClickListener(clickListener)
        background.setOnClickListener(clickListener)
        titleText.setOnClickListener(clickListener)
        descriptionText.setOnClickListener(clickListener)
    }

    private fun navigateToUrl(data: InterstitialData) {
        if (data.targetUrl.isBlank()) {
            Log.d("NotixDebug", "Target url is empty")
            return
        }
        val uri = Uri.parse(data.targetUrl)
        if (data.openExternalBrowser) {
            openBrowser(uri)
        } else {
            openCustomTabsBrowser(uri)
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