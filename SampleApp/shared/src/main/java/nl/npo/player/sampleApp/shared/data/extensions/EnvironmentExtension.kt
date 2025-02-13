package nl.npo.player.sampleApp.shared.data.extensions

import nl.npo.player.library.domain.analytics.model.AnalyticsEnvironment
import nl.npo.player.sampleApp.shared.domain.model.Environment
typealias PlayerEnvironment = nl.npo.player.library.domain.common.enums.Environment

fun Environment.toAnalyticsEnvironment(): AnalyticsEnvironment =
    when (this) {
        Environment.Test -> AnalyticsEnvironment.DEV
        Environment.Acceptance -> AnalyticsEnvironment.PREPROD
        Environment.Production -> AnalyticsEnvironment.PROD
    }

fun Environment.toPlayerEnvironment(): PlayerEnvironment =
    when (this) {
        Environment.Test -> PlayerEnvironment.TEST
        Environment.Acceptance -> PlayerEnvironment.ACC
        Environment.Production -> PlayerEnvironment.PROD
    }
