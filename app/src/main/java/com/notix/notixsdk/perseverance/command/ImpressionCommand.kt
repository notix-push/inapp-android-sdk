package com.notix.notixsdk.perseverance.command

import com.notix.notixsdk.data.EventRepository
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.utils.Mapper
import com.notix.notixsdk.utils.getOrFallback
import org.json.JSONObject

object ImpressionCommand : Command<ImpressionCommand.ImpressionCommandParams>(name = "impression") {
    data class ImpressionCommandParams(val impressionData: String?) : CommandParams

    override val toJsonMapper = object : Mapper<ImpressionCommandParams, JSONObject> {
        override fun map(from: ImpressionCommandParams) = JSONObject().apply {
            put(DATA_FIELD, from.impressionData)
        }
    }

    override val fromJsonMapper = object : Mapper<JSONObject, ImpressionCommandParams> {
        override fun map(from: JSONObject) = ImpressionCommandParams(
            from.getOrFallback<String?>(DATA_FIELD, null)
        )
    }

    override fun provideExecutor() = Executor(SingletonComponent.eventRepository)

    class Executor(
        private val eventRepository: EventRepository
    ) : CommandExecutor() {
        override suspend operator fun invoke(jsonParams: JSONObject): CommandResult {
            val impressionData = fromJsonMapper.map(jsonParams).impressionData ?: return CommandResult.FAILURE
            return when (eventRepository.sendImpression(impressionData)) {
                true -> CommandResult.SUCCESS
                false -> CommandResult.FAILURE
            }
        }
    }

    private const val DATA_FIELD = "impression_data"
}