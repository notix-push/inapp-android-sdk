package co.notix.perseverance.command

import co.notix.utils.Mapper
import org.json.JSONObject

internal sealed class Command<T : CommandParams>(val name: String) {
    internal abstract val fromJsonMapper: Mapper<JSONObject, T>
    abstract val toJsonMapper: Mapper<T, JSONObject>
    abstract fun provideExecutor(): CommandExecutor<T>

    companion object {
        fun findByNameOrNull(name: String) = when (name) {
            ImpressionCommand.name -> ImpressionCommand
            ClickCommand.name -> ClickCommand
            ShowNotificationCommand.name -> ShowNotificationCommand
            else -> null
        }
    }
}

internal abstract class CommandExecutor<T: CommandParams> {
    /**
     * The invocation will not be interrupted. If an exception is thrown during the execution,
     * it will be caught and [CommandResult.FAILURE] will be returned
     */
    abstract suspend operator fun invoke(params: T): CommandResult
}

internal interface CommandParams

internal enum class CommandResult {
    SUCCESS, RETRY, FAILURE
}
