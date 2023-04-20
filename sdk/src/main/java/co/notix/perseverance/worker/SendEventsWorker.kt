package co.notix.perseverance.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.notix.di.SingletonComponent
import co.notix.log.Logger
import co.notix.perseverance.WorkerDataStore
import co.notix.perseverance.command.Command
import co.notix.perseverance.command.CommandParams
import co.notix.perseverance.command.CommandResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class SendEventsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val cs = SingletonComponent.csProvider.provideSupervisedIo()
    private val contextProviderInitializer = SingletonComponent.contextProviderInitializer
    private val workerDataStore = SingletonComponent.workerDataStore

    override suspend fun doWork(): Result {
        Logger.v("worker: begin. runAttemptCount == $runAttemptCount, $this")
        contextProviderInitializer.tryInit(applicationContext)
        while (true) {
            val processingResult = withContext(cs.coroutineContext) {
                // queue processing will not be cancelled in the middle of processOneCommand
                MUTEX.withLock {
                    try {
                        processOneCommand()
                    } catch (e: Exception) {
                        Logger.e("unrecoverable exception, clearing queue")
                        workerDataStore.clearQueue()
                        Result.success()
                    }
                }
            }
            if (processingResult != null) return processingResult
        }
    }

    private suspend fun processOneCommand(): Result? {
        Logger.v("worker: processOneCommand begin $this ")
        val queue = workerDataStore.safeGetQueue()
        Logger.v("worker: queue size = ${queue.list.size}")
        val workerParams = pickCommand(queue) ?: return workerResultIfNoCommand(queue)
        val command = Command.findByNameOrNull(workerParams.type)
            ?: return resultIfUnknownCommandType(workerParams.id)
        when (command.safeExecute(workerParams.paramsJson)) {
            CommandResult.SUCCESS -> {
                Logger.v("worker: command finished with result SUCCESS $workerParams")
                workerDataStore.deleteById(workerParams.id)
            }
            CommandResult.RETRY -> {
                if (workerParams.retryCount >= MAX_TRY_COUNT - 1) {
                    Logger.e("worker: command finished with result RETRY. deleting it because it used too many retries $workerParams")
                    workerDataStore.deleteById(workerParams.id)
                } else {
                    Logger.e("worker: command finished with result RETRY. workerParams = $workerParams")
                    workerDataStore.updateById(workerParams.id, ::addRetry)
                }
            }
            CommandResult.FAILURE -> {
                Logger.e("worker: command finished with result FAILURE. deleting it. workerParams = $workerParams")
                workerDataStore.deleteById(workerParams.id)
            }
        }
        return null
    }

    private fun pickCommand(queue: WorkerDataStore.Queue) =
        queue.list.firstOrNull { isItTimeToExecute(it) }

    private fun workerResultIfNoCommand(queue: WorkerDataStore.Queue): Result {
        val result = if (queue.list.isEmpty()) Result.success() else Result.retry()
        Logger.v("worker: no more executable commands in queue, exiting. result = $result, queue.size = ${queue.list.size}, worker = $this")
        return result
    }

    private suspend fun resultIfUnknownCommandType(commandId: String): Nothing? {
        Logger.e("worker: Unknown command type")
        workerDataStore.deleteById(commandId)
        return null
    }

    private fun isItTimeToExecute(workerParams: WorkerDataStore.WorkerParams) =
        workerParams.nextRetryTime <= System.currentTimeMillis()

    private fun addRetry(workerParams: WorkerDataStore.WorkerParams) = workerParams.copy(
        retryCount = workerParams.retryCount + 1,
        nextRetryTime = System.currentTimeMillis() +
                SendEventsWorkerBackoff.calculateBackoffMillisAsWorkManagerDoes(workerParams.retryCount) - 100
    )

    companion object {
        private val MUTEX = Mutex()
        private const val MAX_TRY_COUNT = 15
    }
}

internal suspend fun <T : CommandParams> Command<T>.safeExecute(jsonObject: JSONObject) =
    runCatching {
        val params = fromJsonMapper.map(jsonObject)
        provideExecutor().invoke(params)
    }.fold(
        onSuccess = { it },
        onFailure = {
            Logger.e(
                msg = "worker: command finished with unexpected exception ${it.message}",
                throwable = it
            )
            CommandResult.FAILURE
        }
    )