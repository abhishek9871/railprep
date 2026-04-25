package com.railprep.core.i18n.di

import com.railprep.core.i18n.LanguageManager
import com.railprep.domain.repository.LanguageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreI18nModule {
    @Binds
    abstract fun bindLanguageRepository(impl: LanguageManager): LanguageRepository
}
