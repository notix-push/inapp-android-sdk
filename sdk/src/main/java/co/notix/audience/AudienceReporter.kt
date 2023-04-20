package co.notix.audience

import co.notix.data.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public interface NotixAudienceReporter {
    public fun add(audience: String)
    public fun delete(audience: String)
}

internal class AudienceReporterImpl(
    private val mainRepository: MainRepository,
    private val csIo: CoroutineScope
) : NotixAudienceReporter {

    override fun add(audience: String) {
        csIo.launch {
            delay(1500) // waiting for init to complete. TODO rewrite
            mainRepository.manageAudience("add", audience)
        }
    }

    override fun delete(audience: String) {
        csIo.launch {
            delay(1500) // waiting for init to complete. TODO rewrite
            mainRepository.manageAudience("delete", audience)
        }
    }
}