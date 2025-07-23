package nl.npo.player.sampleApp.shared.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun <T> LiveData<T>.observeNonNull(
    owner: LifecycleOwner,
    fn: (T) -> Unit,
) {
    observe(owner) { it?.let(fn) }
}
