package nl.npo.player.sample_app.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, fn: (T) -> Unit) {
    observe(owner) { it?.let(fn) }
}
