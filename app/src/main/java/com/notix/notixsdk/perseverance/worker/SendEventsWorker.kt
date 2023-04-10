package com.notix.notixsdk.perseverance.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.perseverance.WorkerDataStore
import com.notix.notixsdk.perseverance.command.Command
import com.notix.notixsdk.perseverance.command.CommandResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SendEventsWorker(
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
        val workerParams = pickCommand(queue) ?: return workerResultIfNoCommand(queue)
        val command = Command.findByNameOrNull(workerParams.type)
            ?: return resultIfUnknownCommandType(workerParams.id)
        val commandResult = safeExecuteCommand(command, workerParams.paramsJson)
        when (commandResult) {
            CommandResult.SUCCESS -> {
                Logger.v("worker: command finished with result SUCCESS $workerParams")
                workerDataStore.deleteById(workerParams.id)
            }
            CommandResult.FAILURE -> {
                if (workerParams.retryCount >= MAX_TRY_COUNT - 1) {
                    Logger.e("worker: command finished with result FAILURE. deleting it because it used too many retries $workerParams")
                    workerDataStore.deleteById(workerParams.id)
                } else {
                    Logger.e("worker: command finished with result FAILURE. workerParams = $workerParams")
                    workerDataStore.updateById(workerParams.id, ::addRetry)
                }
            }
        }
        return null
    }

    private fun pickCommand(queue: WorkerDataStore.Queue) =
        queue.list.firstOrNull { isItTimeToExecute(it) }

    private fun workerResultIfNoCommand(queue: WorkerDataStore.Queue): Result {
        val result = if (queue.list.isEmpty()) Result.success() else Result.retry()
        Logger.v("worker: no more executable commands in queue, exiting. result = $result, worker = $this")
        return result
    }

    private suspend fun resultIfUnknownCommandType(commandId: String): Nothing? {
        Logger.e("worker: Unknown command type")
        workerDataStore.deleteById(commandId)
        return null
    }

    private suspend fun safeExecuteCommand(command: Command<*>, paramsJson: JSONObject) =
        runCatching {
            command.provideExecutor().invoke(paramsJson)
        }.fold(onSuccess = { it }, onFailure = {
            Logger.e(
                msg = "worker: command finished with unexpected exception ${it.message}",
                throwable = it
            )
            CommandResult.FAILURE
        })

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