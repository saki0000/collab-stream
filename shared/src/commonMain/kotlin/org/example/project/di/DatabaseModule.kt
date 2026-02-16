package org.example.project.di

import org.example.project.data.local.AppDatabase
import org.example.project.data.local.DatabaseBuilder
import org.example.project.data.local.FollowedChannelDao
import org.example.project.data.local.SyncHistoryDao
import org.example.project.data.local.UserDeviceDao
import org.example.project.data.repository.ChannelFollowRepositoryImpl
import org.example.project.data.repository.SyncHistoryRepositoryImpl
import org.example.project.data.repository.UserRepositoryImpl
import org.example.project.domain.repository.ChannelFollowRepository
import org.example.project.domain.repository.SyncHistoryRepository
import org.example.project.domain.repository.UserRepository
import org.koin.dsl.module

/**
 * データベース関連の依存性注入モジュール。
 *
 * AppDatabase、DAO、Repositoryの依存関係を定義する。
 * DatabaseBuilderはプラットフォーム固有モジュールで提供される。
 *
 * Story Issue: #36, US-1, US-2
 * Epic: EPIC-003（同期チャンネル履歴保存）, サブスクリプション基盤
 */
val databaseModule = module {
    // AppDatabase（シングルトン）
    single<AppDatabase> {
        get<DatabaseBuilder>().build()
    }

    // DAO
    single<SyncHistoryDao> {
        get<AppDatabase>().syncHistoryDao()
    }

    single<FollowedChannelDao> {
        get<AppDatabase>().followedChannelDao()
    }

    single<UserDeviceDao> {
        get<AppDatabase>().userDeviceDao()
    }

    // Repository
    single<SyncHistoryRepository> {
        SyncHistoryRepositoryImpl(get())
    }

    single<ChannelFollowRepository> {
        ChannelFollowRepositoryImpl(get())
    }

    single<UserRepository> {
        UserRepositoryImpl(get())
    }
}
