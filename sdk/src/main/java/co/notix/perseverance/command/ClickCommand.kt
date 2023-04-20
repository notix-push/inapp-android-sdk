package co.notix.perseverance.command

import co.notix.data.EventRepository
import co.notix.di.SingletonComponent
import co.notix.utils.Mapper
import org.json.JSONObject

internal object ClickCommand : Command<ClickCommand.Params>(name = "click") {
    data class Params(val clickData: String) : CommandParams

    override val toJsonMapper = object : Mapper<Params, JSONObject> {
        override fun map(from: Params) = JSONObject().apply {
            put(DATA_FIELD, from.clickData)
        }
    }

    override val fromJsonMapper = object : Mapper<JSONObject, Params> {
        override fun map(from: JSONObject) = Params(
            clickData = from.getString(DATA_FIELD)
        )
    }

    override fun provideExecutor() = Executor(SingletonComponent.eventRepository)

    class Executor(
        private val eventRepository: EventRepository
    ) : CommandExecutor<Params>() {
        override suspend operator fun invoke(params: Params): CommandResult {
            return when (eventRepository.sendClick(params.clickData)) {
                true -> CommandResult.SUCCESS
                false -> CommandResult.RETRY
            }
        }
    }

    private const val DATA_FIELD = "click_data"
}