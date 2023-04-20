package co.notix.push

import co.notix.push.permissions.NotificationsPermissionController
import co.notix.data.Storage
import co.notix.push.data.PushRepository
import co.notix.push.firebase.FetchFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

internal class UpdateSubscriptionStateUseCase(
    private val fetchFcmTokenUseCase: FetchFcmTokenUseCase,
    private val notificationsPermissionController: NotificationsPermissionController,
    private val storage: Storage,
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