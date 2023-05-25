package nl.npo.player.sample_app.domain.annotation

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
