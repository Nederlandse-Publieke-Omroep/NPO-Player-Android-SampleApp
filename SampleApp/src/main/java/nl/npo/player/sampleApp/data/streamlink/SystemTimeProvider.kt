package nl.npo.player.sampleApp.data.streamlink

import nl.npo.player.sampleApp.domain.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemTimeProvider
    @Inject
    constructor() : TimeProvider {
        override fun currentTimeMillis() = System.currentTimeMillis()
    }
