package com.notix.notixsdk.interstitial

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.notix.notixsdk.R
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.utils.*
import java.lang.ref.WeakReference
import java.net.URL

internal class InterstitialActivity : Activity() {

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
            Log.i("NotixDebug", "data null: $data")
            closeInterstitial(InterstitialContract.RESULT_ERROR)
            return
        }
        composeInterstitial(data)
    }

    private fun prepareActivity() {
        this.window?.let { window ->
            if (Build.VERSION.SDK_INT >= 30) {
                window.setDecorFitsSystemWindows(false)
            } else {
                val decorFitsFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

                val decorView = window.decorView
                val sysUiVis = decorView.systemUiVisibility
                decorView.systemUiVisibility = sysUiVis or decorFitsFlags
            }
            observeInsets(window.decorView)
        }
    }

    private fun observeInsets(view: View) {
        if (Build.VERSION.SDK_INT >= 30) {
            view.setOnApplyWindowInsetsListener { v, insets ->
                val topInset = insets.getInsets(WindowInsets.Type.statusBars()).top
                val rightInset = insets.getInsets(WindowInsets.Type.navigationBars()).right
                applyInsets(topInset, rightInset)
                WindowInsets.CONSUMED
            }

        }
    }

    private fun applyInsets(topInset: Int, rightInset: Int) {
        val layoutParams = (closeButton.layoutParams as? FrameLayout.LayoutParams)
            ?.apply {
                topMargin = topInset
                rightMargin = (8f.dp + rightInset).toInt()
            }
        closeButton.layoutParams = layoutParams
    }

    private fun composeInterstitial(data: InterstitialData) {
        loadImage(data.imageUrl) {
            runOnUiThread {
                it?.let { drawable ->
                    progressView.visibility = View.INVISIBLE
                    image.setImageDrawable(drawable)
                    setBlurredBackground(drawable)
                }
                configureControls(data, it)
            }
            apiClient.impression(this, data.impressionData)
        }
    }

    private fun loadImage(url: String, onResult: (Drawable?) -> Unit) {
        Thread {
            try {
                val uriStream = URL(url).openStream()
                val bmp = BitmapFactory.decodeStream(uriStream)

                onResult(BitmapDrawable(resources, bmp))
            } catch (e: Throwable) {

                Log.i("NotixDebug", "loadImage error: $e")
                onResult(null)
            }
        }.start()
    }

    private fun setBlurredBackground(image: Drawable?) {
        if (image == null) return

        background.setImageBitmap(blur(this, image.toBitmap()))
    }


    // from coil
    private fun blur(context: Context, input: Bitmap): Bitmap {
        val radius = 10f
        val sampling = 1f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val scaledWidth = (input.width / sampling).toInt()
        val scaledHeight = (input.height / sampling).toInt()
        val output =
            Bitmap.createBitmap(scaledWidth, scaledHeight, input.config ?: Bitmap.Config.ARGB_8888)
        Canvas(output).apply {
            scale(1 / sampling, 1 / sampling)
            drawBitmap(input, 0f, 0f, paint)
        }

        var script: RenderScript? = null
        var tmpInt: Allocation? = null
        var tmpOut: Allocation? = null
        var blur: ScriptIntrinsicBlur? = null
        try {
            script = RenderScript.create(context)
            tmpInt = Allocation.createFromBitmap(
                script,
                output,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )
            tmpOut = Allocation.createTyped(script, tmpInt.type)
            blur = ScriptIntrinsicBlur.create(script, Element.U8_4(script))
            blur.setRadius(radius)
            blur.setInput(tmpInt)
            blur.forEach(tmpOut)
            tmpOut.copyTo(output)
        } finally {
            script?.destroy()
            tmpInt?.destroy()
            tmpOut?.destroy()
            blur?.destroy()
        }

        return output
    }

    private fun configureControls(data: InterstitialData, drawable: Drawable?) {
        ColorPairsGenerator.generate(this, drawable) { colors ->
            // use primary color for button if it exists for text, otherwise
            val textColors = if (data.buttons.isNotEmpty()) {
                colors.getOrNull(1) ?: colors.first()
            } else {
                colors.first()
            }
            val buttonColors = colors.first()
            runOnUiThread {
                drawTexts(data, textColors)
                drawButtons(data, buttonColors)
                configureCloseButton(data)
                setCloseListener()
                setClickListener(data)
            }
        }
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
        val customTextColor = Colors.parseColor(buttonSpec.textColor)
        val customBackgroundColor = Colors.parseColor(buttonSpec.backgroundColor)
        val buttonTextColor = customTextColor ?: colorsPair.first
        val buttonBackgroundColor = customBackgroundColor ?: colorsPair.second
        val buttonBackground = generateBackgroundDrawable(buttonBackgroundColor, 16.dp)
        with(button) {
            visibility = View.VISIBLE
            text = buttonSpec.text
            background = buttonBackground
            setTextColor(buttonTextColor)
        }
    }

    private fun generateBackgroundDrawable(color: Int, radius: Float) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }

    private fun configureCloseButton(data: InterstitialData) {
        val closingSettings = data.closingSettings
        val showButtonTime = closingSettings.timeout * 1000L
        val buttonWR = WeakReference(closeButton)

        val timer = object : CountDownTimer(showButtonTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                buttonWR.get()?.visibility = View.VISIBLE
            }
        }

        timer.start()

        if (closingSettings.opacity in 0f..1f) {
            closeButton.alpha = closingSettings.opacity
        }

        val sizeMultiplier = closingSettings.sizePercent
        if (sizeMultiplier in 0f..2f) {
            val layoutParams = (closeButton.layoutParams as? FrameLayout.LayoutParams)
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