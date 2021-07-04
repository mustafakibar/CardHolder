package kibar.cardholder.di

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kibar.cardholder.ui.cardview.CardViewHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideMLBcrCapture(): MLBcrCapture =
        MLBcrCaptureFactory.getInstance().getBcrCapture(
            MLBcrCaptureConfig.Factory()
                .setOrientation(MLBcrCaptureConfig.ORIENTATION_AUTO)
                .create()
        )

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(
            "data",
            AppCompatActivity.MODE_PRIVATE
        )

    @Provides
    @Singleton
    fun provideCardViewAndEditHelper(@ApplicationContext context: Context) = CardViewHelper(context)

}