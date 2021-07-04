package kibar.cardholder.di

import android.content.Context
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kibar.cardholder.ui.cardview.CardViewHelper

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityModule {

    @Provides
    fun provideAGConnectServicesConfig(@ApplicationContext context: Context): AGConnectServicesConfig =
        AGConnectServicesConfig.fromContext(context)

    @Provides
    fun provideAsrRecognizer(@ApplicationContext context: Context): MLAsrRecognizer =
        MLAsrRecognizer.createAsrRecognizer(context)

    @Provides
    fun provideCardViewAndEditHelper(@ApplicationContext context: Context): CardViewHelper =
        CardViewHelper(context)

}