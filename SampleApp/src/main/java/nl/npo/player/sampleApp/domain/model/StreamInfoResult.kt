package nl.npo.player.sampleApp.domain.model

sealed class StreamInfoResult<out R> {
    data class Success<out T>(val data: T) : StreamInfoResult<T>()

    data class Error(val exception: Exception) : StreamInfoResult<Nothing>()
}
