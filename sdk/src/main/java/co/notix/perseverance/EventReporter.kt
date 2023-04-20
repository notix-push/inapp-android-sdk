package co.notix.perseverance

import co.notix.perseverance.command.ClickCommand
import co.notix.perseverance.command.ImpressionCommand
import co.notix.perseverance.worker.SendEventsWorkerLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface EventReporter {
    fun reportImpression(data: String)
    fun reportClick(data: String)
}

internal class EventReporterImpl(
    private val workerDataStore: WorkerDataStore,
    private val workerLauncher: SendEventsWorkerLauncher,
    private val cs: CoroutineScope
) : EventReporter {
    override fun reportImpression(data: String) {
        cs.launch {
            workerDataStore.addCommand(
                ImpressionCommand,
                ImpressionCommand.Params(impressionData = data)
            )
            workerLauncher.launch()
        }
    }

    override fun reportClick(data: String) {
        cs.launch {
            workerDataStore.addCommand(
                ClickCommand,
                ClickCommand.Params(clickData = data)
            )
            workerLauncher.launch()
        }
    }
}