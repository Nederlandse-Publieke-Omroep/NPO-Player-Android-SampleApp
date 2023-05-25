package nl.npo.player.sample_app.data.streamlink

import nl.npo.player.sample_app.domain.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun currentTimeMillis() = System.currentTimeMillis()
}
