package nl.npo.player.sampleApp.presentation.ext

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class ByteUnit(
    val suffix: String,
) {
    BYTES("B"),
    KIBI("KiB"),
    MEBI("MiB"),
    GIBI("GiB"),
    TEBI("TiB"),
    PEBI("PiB"),
    EXBI("EiB"),
}

private const val BYTES_PER_UNIT = 1024.0

/**
 * Formats a byte count as a human-readable size, e.g. 4_404_019L -> "4.2 MiB".
 */
fun Long.toHumanReadableSize(locale: Locale = Locale.US): String {
    require(this >= 0) { "Byte count must not be negative, was $this" }

    var remaining = toDouble()
    var unit = ByteUnit.BYTES

    for (candidate in ByteUnit.entries) {
        if (remaining < BYTES_PER_UNIT) {
            unit = candidate
            break
        }
        remaining /= BYTES_PER_UNIT
    }

    val formatter = DecimalFormat("#.##", DecimalFormatSymbols(locale))
    return "${formatter.format(remaining)} ${unit.suffix}"
}
