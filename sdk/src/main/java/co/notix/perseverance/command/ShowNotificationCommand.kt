package co.notix.perseverance.command

import co.notix.di.SingletonComponent
import co.notix.push.NotificationHandler
import co.notix.push.data.Notification
import co.notix.push.data.jsonToNotificationMapper
import co.notix.push.data.notificationToJsonMapper
import co.notix.utils.Mapper
import org.json.JSONObject

internal object ShowNotificationCommand :
    Command<ShowNotificationCommand.Params>(name = "showNotification") {
    data class Params(val notification: Notification) : CommandParams

    override val fromJsonMapper = object : Mapper<JSONObject, Params> {
        override fun map(from: JSONObject) = Params(
            notification = jsonToNotificationMapper.map(from.getJSONObject(NOTIFICATION_FIELD))
        )
    }
    override val toJsonMapper = object : Mapper<Params, JSONObject> {
        override fun map(from: Params) = JSONObject().apply {
            put(NOTIFICATION_FIELD, notificationToJsonMapper.map(from.notification))
        }
    }

    override fun provideExecutor() = Executor(SingletonComponent.notificationHandler)

    class Executor(
        private val notificationHandler: NotificationHandler
    ) : CommandExecutor<Params>() {
        override suspend operator fun invoke(params: Params): CommandResult {
            return notificationHandler.handleNotification(params.notification)
        }
    }

    private const val NOTIFICATION_FIELD = "notification"
}