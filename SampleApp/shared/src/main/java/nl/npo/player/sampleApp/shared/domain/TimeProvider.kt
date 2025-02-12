package nl.npo.player.sampleApp.shared.domain

import java.util.concurrent.TimeUnit

interface TimeProvider {
    fun currentTimeMillis(): Long

    fun currentTimeSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis())
}
