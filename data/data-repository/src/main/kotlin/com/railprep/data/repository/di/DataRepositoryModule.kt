package com.railprep.data.repository.di

import com.railprep.data.repository.AttemptRepositoryImpl
import com.railprep.data.repository.AuthRepositoryImpl
import com.railprep.data.repository.BookmarkRepositoryImpl
import com.railprep.data.repository.DigestRepositoryImpl
import com.railprep.data.repository.LearnRepositoryImpl
import com.railprep.data.repository.ProfileRepositoryImpl
import com.railprep.data.repository.TestsRepositoryImpl
import com.railprep.domain.repository.AttemptRepository
import com.railprep.domain.repository.AuthRepository
import com.railprep.domain.repository.BookmarkRepository
import com.railprep.domain.repository.DigestRepository
import com.railprep.domain.repository.LearnRepository
import com.railprep.domain.repository.ProfileRepository
import com.railprep.domain.repository.TestsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindLearnRepository(impl: LearnRepositoryImpl): LearnRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindTestsRepository(impl: TestsRepositoryImpl): TestsRepository

    @Binds
    @Singleton
    abstract fun bindAttemptRepository(impl: AttemptRepositoryImpl): AttemptRepository

    @Binds
    @Singleton
    abstract fun bindDigestRepository(impl: DigestRepositoryImpl): DigestRepository
}
