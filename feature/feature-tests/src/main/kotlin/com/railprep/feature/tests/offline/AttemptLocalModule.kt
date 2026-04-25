package com.railprep.feature.tests.offline

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttemptLocalModule {

    @Provides
    @Singleton
    fun provideAttemptLocalDatabase(@ApplicationContext ctx: Context): AttemptLocalDatabase =
        Room.databaseBuilder(ctx, AttemptLocalDatabase::class.java, "railprep-attempts.db").build()

    @Provides
    fun provideAttemptLocalDao(db: AttemptLocalDatabase): AttemptLocalDao = db.dao()
}
