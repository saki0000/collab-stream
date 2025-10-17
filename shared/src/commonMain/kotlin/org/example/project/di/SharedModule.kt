package org.example.project.di

import org.example.project.data.datasource.TwitchSearchDataSource
import org.example.project.data.datasource.TwitchSearchDataSourceImpl
import org.example.project.data.datasource.YouTubeSearchDataSource
import org.example.project.data.datasource.YouTubeSearchDataSourceImpl
import org.example.project.data.repository.VideoSearchRepositoryImpl
import org.example.project.data.repository.VideoSyncRepositoryImpl
import org.example.project.domain.repository.VideoSearchRepository
import org.example.project.domain.repository.VideoSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase
import org.example.project.domain.usecase.VideoSearchUseCase
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.domain.usecase.VideoSyncUseCaseImpl
import org.koin.dsl.module

val sharedModule = module {

    // HTTP Client
    single {
        VideoSyncRepositoryImpl.createHttpClient()
    }

    // Repository bindings
    single<VideoSyncRepository> {
        VideoSyncRepositoryImpl(get())
    }

    single<VideoSearchRepository> {
        VideoSearchRepositoryImpl(get(), get())
    }

    // Data source bindings
    single<YouTubeSearchDataSource> {
        YouTubeSearchDataSourceImpl(get())
    }

    single<TwitchSearchDataSource> {
        TwitchSearchDataSourceImpl(get())
    }

    // Use case bindings
    single<VideoSyncUseCase> {
        VideoSyncUseCaseImpl(get())
    }

    single<VideoSearchUseCase> {
        VideoSearchUseCase(get())
    }

    single<ChannelSearchUseCase> {
        ChannelSearchUseCase(get())
    }
}
