package nl.npo.player.sampleApp.presentation.ext

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import nl.npo.player.library.data.extensions.isThisDeviceATelevision
import nl.npo.player.library.domain.extensions.tryCatchOrNull

fun Context.isGooglePlayServicesAvailable(): Boolean {
    return tryCatchOrNull {
        GoogleApiAvailability
            .getInstance()
            .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
    } ?: false
}

val Context.supportsCasting: Boolean get() = isGooglePlayServicesAvailable() && !isThisDeviceATelevision()
