package com.notix.notixsdk.push

import com.notix.notixsdk.push.permissions.NotificationsPermissionController
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.push.data.PushRepository
import com.notix.notixsdk.push.firebase.FetchFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

class UpdateSubscriptionStateUseCase(
    private val fetchFcmTokenUseCase: FetchFcmTokenUseCase,
    private val notificationsPermissionController: NotificationsPermissionController,
    private val storage: StorageProvider,
    private val pushRepository: PushRepository,
    private val cs: CoroutineScope,
) {
    suspend operator fun invoke() = withContext(cs.coroutineContext) {
        val isSubscriptionActive = storage.isSubscriptionActive()
        val oldFcmToken = storage.getFcmToken()
        val newFcmToken = fetchFcmTokenUseCase() ?: return@withContext
        val canPost = notificationsPermissionController.updateAndGetCanPostNotifications()
        when {
            newFcmToken != oldFcmToken && canPost ->
                pushRepository.subscribe(newFcmToken)
            isSubscriptionActive && !canPost ->
                pushRepository.unsubscribe()
        }
    }
}