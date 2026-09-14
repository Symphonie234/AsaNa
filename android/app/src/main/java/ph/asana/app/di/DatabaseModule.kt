package ph.asana.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ph.asana.app.data.local.AsaNaDatabase
import ph.asana.app.data.local.CacheDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AsaNaDatabase =
        Room.databaseBuilder(context, AsaNaDatabase::class.java, "asana.db").build()

    @Provides
    fun provideCacheDao(database: AsaNaDatabase): CacheDao = database.cacheDao()
}
