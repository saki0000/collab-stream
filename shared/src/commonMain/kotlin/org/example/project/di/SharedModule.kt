package org.example.project.di

import org.example.project.data.repository.CommentRepositoryImpl
import org.example.project.data.repository.TimelineSyncRepositoryImpl
import org.example.project.data.repository.VideoSearchRepositoryImpl
import org.example.project.data.repository.VideoSyncRepositoryImpl
import org.example.project.domain.repository.CommentRepository
import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.repository.VideoSearchRepository
import org.example.project.domain.repository.VideoSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase
import org.example.project.domain.usecase.VideoSearchUseCase
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.domain.usecase.VideoSyncUseCaseImpl
import org.example.project.SERVER_BASE_URL
import org.koin.dsl.module

/**
 * Shared モジュールの DI 定義
 *
 * ADR-005 Phase 2: DataSource を除去し、サーバーAPI経由の実装に移行。
 * すべての Repository が HttpClient を通じてサーバーAPIを呼び出す。
 */
val sharedModule = module {

    // HTTP Client
    single {
        VideoSyncRepositoryImpl.createHttpClient()
    }

    // Repository bindings
    single<VideoSyncRepository> {
        VideoSyncRepositoryImpl(get())
    }

    single<TimelineSyncRepository> {
        TimelineSyncRepositoryImpl(get())
    }

    single<VideoSearchRepository> {
        VideoSearchRepositoryImpl(get())
    }

    single<CommentRepository> {
        CommentRepositoryImpl(get(), SERVER_BASE_URL)
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
