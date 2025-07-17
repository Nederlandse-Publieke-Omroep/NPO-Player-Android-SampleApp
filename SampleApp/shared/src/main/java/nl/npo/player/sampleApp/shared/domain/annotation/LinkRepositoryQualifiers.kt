package nl.npo.player.sampleApp.shared.domain.annotation

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamLinkRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class URLLinkRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfflineLinkRepository
