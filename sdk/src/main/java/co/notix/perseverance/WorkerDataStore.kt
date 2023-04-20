package co.notix.perseverance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.notix.di.contextprovider.ContextProvider
import co.notix.log.Logger
import co.notix.perseverance.command.Command
import co.notix.perseverance.command.CommandParams
import co.notix.utils.Mapper
import co.notix.utils.toList
import java.util.UUID
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

internal class WorkerDataStore(
    private val contextProvider: ContextProvider
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "worker")
    private val queueKey = stringPreferencesKey("queue")

    suspend fun safeGetQueue() = context.dataStore.safeReadQueue()

    suspend fun deleteById(id: String) {
        context.dataStore.edit { prefs ->
            val prevQueue = prefs.safeReadQueue()
            val newQueue = prevQueue.copy(
                list = prevQueue.list.toMutableList().apply {
                    removeAll { it.id == id }
                }
            )
            prefs[queueKey] = queueToJsonMapper.map(newQueue).toString()
        }
    }

    suspend fun updateById(id: String, action: (WorkerParams) -> WorkerParams) {
        context.dataStore.edit { prefs ->
            val prevQueue = prefs.safeReadQueue()
            val newQueue = prevQueue.copy(
                list = prevQueue.list.toMutableList().map {
                    if (it.id == id) {
                        action(it)
                    } else {
                        it
                    }
                }
            )
            prefs[queueKey] = queueToJsonMapper.map(newQueue).toString()
        }
    }

    internal suspend fun addCommand(commandName: String, commandParams: JSONObject) {
        context.dataStore.edit { prefs ->
            val prevQueue = prefs.safeReadQueue()
            val newQueue = prevQueue.copy(
                list = prevQueue.list.toMutableList().apply {
                    add(
                        WorkerParams(
                            id = UUID.randomUUID().toString(),
                            type = commandName,
                            retryCount = 0,
                            nextRetryTime = System.currentTimeMillis(),
                            paramsJson = commandParams
                        )
                    )
                }
            )
            prefs[queueKey] = queueToJsonMapper.map(newQueue).toString()
        }
    }

    private suspend fun DataStore<Preferences>.safeReadQueue() = runCatching {
        this.data.first()[queueKey]
            ?.let { JSONArray(it) }
            ?.let { fromJsonToQueueMapper.map(it) }
    }.fold(
        onSuccess = { it ?: Queue(emptyList()) },
        onFailure = {
            Logger.e(
                msg = "worker: error while reading queue ${it.message}. Clearing queue!",
                throwable = it
            )
            edit { prefs -> prefs.remove(queueKey) }
            Queue(emptyList())
        }
    )

    private fun MutablePreferences.safeReadQueue() = runCatching {
        get(queueKey)
            ?.let { JSONArray(it) }
            ?.let { fromJsonToQueueMapper.map(it) }
    }.fold(
        onSuccess = { it ?: Queue(emptyList()) },
        onFailure = {
            Logger.e(
                msg = "worker: error while reading queue ${it.message}. Clearing queue!",
                throwable = it
            )
            clear()
            Queue(emptyList())
        }
    )

    private val queueToJsonMapper = object : Mapper<Queue, JSONArray> {
        override fun map(from: Queue) = JSONArray().also { array ->
            from.list.forEach { workerParams ->
                array.put(JSONObject().apply {
                    put(WORKER_KEY_ID, workerParams.id)
                    put(WORKER_KEY_TYPE, workerParams.type)
                    put(WORKER_KEY_RETRY_COUNT, workerParams.retryCount)
                    put(WORKER_KEY_NEXT_RETRY_TIME, workerParams.nextRetryTime)
                    put(WORKER_KEY_PARAMS_JSON, workerParams.paramsJson)
                })
            }
        }
    }
    private val fromJsonToQueueMapper = object : Mapper<JSONArray, Queue> {
        override fun map(from: JSONArray) =
            from.toList<JSONObject>().mapNotNull { workerParamsJson ->
                runCatching {
                    WorkerParams(
                        id = workerParamsJson.getString(WORKER_KEY_ID),
                        type = workerParamsJson.getString(WORKER_KEY_TYPE),
                        retryCount = workerParamsJson.getInt(WORKER_KEY_RETRY_COUNT),
                        nextRetryTime = workerParamsJson.getLong(WORKER_KEY_NEXT_RETRY_TIME),
                        paramsJson = workerParamsJson.getJSONObject(WORKER_KEY_PARAMS_JSON)
                    )
                }.getOrNull()
            }.let { Queue(it) }
    }

    suspend fun clearQueue() = context.dataStore.edit { prefs -> prefs.clear() }

    data class WorkerParams(
        val id: String,
        val type: String,
        val retryCount: Int,
        val nextRetryTime: Long,
        val paramsJson: JSONObject
    )

    data class Queue(val list: List<WorkerParams>)

    private val context
        get() = contextProvider.appContext
}

internal suspend inline fun <P : CommandParams> WorkerDataStore.addCommand(
    command: Command<P>,
    params: P
) = addCommand(commandName = command.name, commandParams = command.toJsonMapper.map(params))

private const val WORKER_KEY_TYPE = "type"
private const val WORKER_KEY_ID = "id"
private const val WORKER_KEY_RETRY_COUNT = "retryCount"
private const val WORKER_KEY_NEXT_RETRY_TIME = "nextRetryTime"
private const val WORKER_KEY_PARAMS_JSON = "paramsJson"