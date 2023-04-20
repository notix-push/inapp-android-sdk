package co.notix.network

import android.webkit.WebSettings
import co.notix.di.contextprovider.ContextProvider
import co.notix.data.Storage
import java.util.Locale
import okhttp3.OkHttpClient

internal class OkHttpClientProvider {
    fun provide(contextProvider: ContextProvider, storage: Storage) =
        OkHttpClient.Builder().addInterceptor {
            val userAgent = storage.getCustomUserAgent()
                ?: WebSettings.getDefaultUserAgent(contextProvider.appContext)
            val request = it.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept-Language", Locale.getDefault().toLanguageTag())
                .addHeader("User-Agent", userAgent)
                .build()
            it.proceed(request)
        }.build()
}