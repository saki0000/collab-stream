package org.example.project.testing.di

import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.repository.VideoSearchRepository
import org.example.project.domain.repository.VideoSyncRepository
import org.example.project.testing.repository.FakeTimelineSyncRepository
import org.example.project.testing.repository.FakeVideoSearchRepository
import org.example.project.testing.repository.FakeVideoSyncRepository
import org.koin.dsl.module

/**
 * テスト用Koinモジュール。
 *
 * 本番のRepository実装をFake実装に置き換える。
 * テストクラスでこのモジュールをロードして使用する。
 *
 * 使用例:
 * ```kotlin
 * class MyViewModelTest {
 *     @BeforeTest
 *     fun setup() {
 *         startKoin {
 *             modules(testModule)
 *         }
 *     }
 *
 *     @AfterTest
 *     fun tearDown() {
 *         stopKoin()
 *     }
 *
 *     @Test
 *     fun `test something`() {
 *         val fakeRepo = get<FakeTimelineSyncRepository>()
 *         fakeRepo.channelVideosToReturn = VideoDetailsTestData.createYouTubeVideoDetailsList()
 *         // テスト実行
 *     }
 * }
 * ```
 */
val testModule =
    module {
        // Fake Repository as singletons (same instance throughout test)
        single<FakeVideoSyncRepository> { FakeVideoSyncRepository() }
        single<FakeTimelineSyncRepository> { FakeTimelineSyncRepository() }
        single<FakeVideoSearchRepository> { FakeVideoSearchRepository() }

        // Bind interfaces to Fake implementations
        single<VideoSyncRepository> { get<FakeVideoSyncRepository>() }
        single<TimelineSyncRepository> { get<FakeTimelineSyncRepository>() }
        single<VideoSearchRepository> { get<FakeVideoSearchRepository>() }
    }
