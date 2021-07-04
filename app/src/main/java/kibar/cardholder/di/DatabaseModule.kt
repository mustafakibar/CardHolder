package kibar.cardholder.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kibar.cardholder.db.AppDatabase
import kibar.cardholder.data.bankcard.BankCardDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        ).allowMainThreadQueries().build()
    }

    @Provides
    @Singleton
    fun provideBankCardDao(appDatabase: AppDatabase): BankCardDao =
        appDatabase.bankCardDao

}