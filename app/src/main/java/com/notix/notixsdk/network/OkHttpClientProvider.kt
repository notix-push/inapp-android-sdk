package com.notix.notixsdk.network

import android.webkit.WebSettings
import com.notix.notixsdk.di.contextprovider.ContextProvider
import com.notix.notixsdk.providers.StorageProvider
import java.util.Locale
import okhttp3.OkHttpClient

class OkHttpClientProvider {
    fun provide(contextProvider: ContextProvider, storage: StorageProvider) =
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