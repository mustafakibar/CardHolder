package kibar.cardholder.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kibar.cardholder.api.HMSAuthApiService
import kibar.cardholder.data.bankcard.BankCardDao
import kibar.cardholder.data.bankcard.BankCardRepository
import kibar.cardholder.data.bin.BinApiRepositoryImpl
import kibar.cardholder.data.bin.BindApiRepository
import kibar.cardholder.data.search.SearchKitWebSearchDaoImpl
import kibar.cardholder.data.search.WebSearchDao
import kibar.cardholder.data.search.WebSearchRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideBinApiRepository(repository: BinApiRepositoryImpl): BindApiRepository

    companion object {
        @Provides
        @Singleton
        fun provideBankCardRepository(dao: BankCardDao) =
            BankCardRepository(dao)

        @Provides
        @Singleton
        fun provideWebSearchDao(
            @ApplicationContext context: Context,
            hmsAuthApiService: HMSAuthApiService
        ): WebSearchDao =
            SearchKitWebSearchDaoImpl(context, hmsAuthApiService)

        @Provides
        @Singleton
        fun provideWebSearchRepository(dao: WebSearchDao) =
            WebSearchRepository(dao)
    }

}