package nl.npo.player.sample_app.domain

import java.util.concurrent.TimeUnit

interface TimeProvider {
    fun currentTimeMillis(): Long

    fun currentTimeSeconds(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis())
    }
}
