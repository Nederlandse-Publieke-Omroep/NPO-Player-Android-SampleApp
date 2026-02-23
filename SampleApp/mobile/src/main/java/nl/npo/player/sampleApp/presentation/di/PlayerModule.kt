package nl.npo.player.sampleApp.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.presentation.player.NPOPlayerFactory
import nl.npo.player.sampleApp.presentation.player.NPOPlayerFactoryImpl
import nl.npo.player.sampleApp.presentation.player.PlayerRepository
import nl.npo.player.sampleApp.presentation.player.PlayerRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    abstract fun bindPlayerRepository(
        impl: PlayerRepositoryImpl
    ): PlayerRepository

    @Binds
    abstract fun bindOurPlayerFactory(
        impl: NPOPlayerFactoryImpl
    ): NPOPlayerFactory
}
