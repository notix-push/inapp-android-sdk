package com.notix.notixsdk.perseverance.command

import com.notix.notixsdk.data.EventRepository
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.utils.Mapper
import com.notix.notixsdk.utils.getOrFallback
import org.json.JSONObject

object ClickCommand : Command<ClickCommand.ClickCommandParams>(name = "click") {
    data class ClickCommandParams(val clickData: String?) : CommandParams

    override val toJsonMapper = object : Mapper<ClickCommandParams, JSONObject> {
        override fun map(from: ClickCommandParams) = JSONObject().apply {
            put(DATA_FIELD, from.clickData)
        }
    }

    override val fromJsonMapper = object : Mapper<JSONObject, ClickCommandParams> {
        override fun map(from: JSONObject) = ClickCommandParams(
            from.getOrFallback<String?>(DATA_FIELD, null)
        )
    }

    override fun provideExecutor() = Executor(SingletonComponent.eventRepository)

    class Executor(
        private val eventRepository: EventRepository
    ) : CommandExecutor() {
        override suspend operator fun invoke(jsonParams: JSONObject): CommandResult {
            val clickData = fromJsonMapper.map(jsonParams).clickData ?: return CommandResult.FAILURE
            return when (eventRepository.sendClick(clickData)) {
                true -> CommandResult.SUCCESS
                false -> CommandResult.FAILURE
            }
        }
    }

    private const val DATA_FIELD = "click_data"
}