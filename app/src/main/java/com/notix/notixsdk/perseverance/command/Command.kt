package com.notix.notixsdk.perseverance.command

import com.notix.notixsdk.utils.Mapper
import org.json.JSONObject

sealed class Command<T : CommandParams>(val name: String) {
    internal abstract val fromJsonMapper: Mapper<JSONObject, T>
    abstract val toJsonMapper: Mapper<T, JSONObject>
    abstract fun provideExecutor(): CommandExecutor

    companion object {
        fun findByNameOrNull(name: String) = when (name) {
            ImpressionCommand.name -> ImpressionCommand
            ClickCommand.name -> ClickCommand
            else -> null
        }
    }
}

abstract class CommandExecutor {
    abstract suspend operator fun invoke(jsonParams: JSONObject): CommandResult
}

interface CommandParams

enum class CommandResult {
    SUCCESS, FAILURE
}
