package org.example.project.di

import org.example.project.data.repository.VideoSyncRepositoryImpl
import org.example.project.domain.repository.VideoSyncRepository
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.domain.usecase.VideoSyncUseCaseImpl
import org.koin.dsl.module

val sharedModule = module {

    // Repository bindings
    single<VideoSyncRepository> {
        VideoSyncRepositoryImpl()
    }
    // Use case bindings
    single<VideoSyncUseCase> {
        VideoSyncUseCaseImpl(get())
    }
}
