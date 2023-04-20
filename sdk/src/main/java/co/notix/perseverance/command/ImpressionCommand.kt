package co.notix.perseverance.command

import co.notix.data.EventRepository
import co.notix.di.SingletonComponent
import co.notix.utils.Mapper
import co.notix.utils.mapOrNull
import org.json.JSONObject

internal object ImpressionCommand : Command<ImpressionCommand.Params>(name = "impression") {
    data class Params(val impressionData: String) : CommandParams

    override val toJsonMapper = object : Mapper<Params, JSONObject> {
        override fun map(from: Params) = JSONObject().apply {
            put(DATA_FIELD, from.impressionData)
        }
    }

    override val fromJsonMapper = object : Mapper<JSONObject, Params> {
        override fun map(from: JSONObject) = Params(
            impressionData = from.getString(DATA_FIELD)
        )
    }

    override fun provideExecutor() = Executor(SingletonComponent.eventRepository)

    class Executor(
        private val eventRepository: EventRepository
    ) : CommandExecutor<Params>() {
        override suspend operator fun invoke(params: Params): CommandResult {
            return when (eventRepository.sendImpression(params.impressionData)) {
                true -> CommandResult.SUCCESS
                false -> CommandResult.RETRY
            }
        }
    }

    private const val DATA_FIELD = "impression_data"
}