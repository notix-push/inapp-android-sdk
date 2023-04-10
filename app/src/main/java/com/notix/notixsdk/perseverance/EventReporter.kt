package com.notix.notixsdk.perseverance

import com.notix.notixsdk.perseverance.command.ClickCommand
import com.notix.notixsdk.perseverance.command.ImpressionCommand
import com.notix.notixsdk.perseverance.worker.SendEventsWorkerLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface EventReporter {
    fun reportImpression(data: String)
    fun reportClick(data: String)
}

class EventReporterImpl(
    private val workerDataStore: WorkerDataStore,
    private val workerLauncher: SendEventsWorkerLauncher,
    private val cs: CoroutineScope
) : EventReporter {
    override fun reportImpression(data: String) {
        cs.launch {
            workerDataStore.addCommand(
                commandName = ImpressionCommand.name,
                commandParams = ImpressionCommand.toJsonMapper.map(
                    ImpressionCommand.ImpressionCommandParams(data)
                )
            )
            workerLauncher.launch()
        }
    }

    override fun reportClick(data: String) {
        cs.launch {
            workerDataStore.addCommand(
                commandName = ClickCommand.name,
                commandParams = ClickCommand.toJsonMapper.map(
                    ClickCommand.ClickCommandParams(data)
                )
            )
            workerLauncher.launch()
        }
    }
}