package co.notix.push.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import co.notix.R
import co.notix.di.contextprovider.ContextProvider
import co.notix.log.Logger
import co.notix.data.Storage

internal class FirebaseInstanceProvider(
    private val contextProvider: ContextProvider,
    private val storage: Storage
) {
    fun get(): FirebaseApp? {
        val senderId = storage.getSenderId()
        if (senderId == 0L) {
            Logger.i("Fetching sender id failed")
            return null
        }
        val context = contextProvider.appContext
        val getString: (Int) -> String = context.resources::getString
        val appName = getString(R.string.notix_public_app_name)
        val appId = getString(R.string.notix_public_app_id)
        val apiKey = getString(R.string.notix_public_api_key)
        val projectId = getString(R.string.notix_public_project_id)
        return FirebaseApp.getApps(context).find { it.name == appName }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setGcmSenderId(senderId.toString())
                    .setApplicationId(appId)
                    .setApiKey(apiKey)
                    .setProjectId(projectId)
                    .build(),
                appName
            )
    }

    fun getFcm() = get()?.get(FirebaseMessaging::class.java)
}