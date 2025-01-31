package nl.npo.player.sampleApp.shared.model

import nl.npo.player.library.domain.streamLink.model.Nicam
import nl.npo.player.library.domain.streamLink.model.NicamContentDescription
import java.io.Serializable

@kotlinx.serialization.Serializable
data class MyNicamContentDescription(
    override val ageRating: Nicam.Age = Nicam.Age.AGE_NOT_YET_RATED,
    override val warnings: List<Nicam.Warning> = emptyList(),
) : NicamContentDescription,
    Serializable
