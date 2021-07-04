package kibar.cardholder.ui.cardview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kibar.cardholder.model.BankCard
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class CardViewHelper @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        const val RESULT_REQUEST_CODE = 155
        const val INTENT_RESULT_KEY_NAME = "BANK_CARD_RESULT"

        private const val LAST_SCANNED_BANK_CARD_FILE_NAME = "lastScannedBankCard.jpg"

        private const val INTENT_EXTRA_NAME_BCR_TYPE = "BCR_TYPE"
        private const val INTENT_EXTRA_NAME_BCR_NUMBER = "BCR_NUMBER"
        private const val INTENT_EXTRA_NAME_BCR_EXPIRE = "BCR_EXPIRE"
        private const val INTENT_EXTRA_NAME_BCR_ISSUER = "BCR_ISSUER"
        private const val INTENT_EXTRA_NAME_BCR_ORGANIZATION = "BCR_ORGANIZATION"
    }

    fun startActivityForResult(activity: Activity, captureResult: MLBcrCaptureResult) {
        FileOutputStream(getLastScannedBankCardFile(activity))
            .use { captureResult.originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

        activity.startActivityForResult(
            Intent(
                activity,
                CardViewActivity::class.java
            ).putMLBcrCaptureResult(captureResult),
            RESULT_REQUEST_CODE
        )
    }

    private fun getLastScannedBankCardFile(context: Context) =
        File(context.cacheDir, LAST_SCANNED_BANK_CARD_FILE_NAME)

    private fun Intent.putMLBcrCaptureResult(captureResult: MLBcrCaptureResult) = apply {
        putExtra(INTENT_EXTRA_NAME_BCR_NUMBER, captureResult.number)
        putExtra(INTENT_EXTRA_NAME_BCR_EXPIRE, captureResult.expire)
        putExtra(INTENT_EXTRA_NAME_BCR_ISSUER, captureResult.issuer)
        putExtra(INTENT_EXTRA_NAME_BCR_ORGANIZATION, captureResult.organization)
        putExtra(INTENT_EXTRA_NAME_BCR_TYPE, captureResult.type)
    }

    fun createMLBcrCaptureResultFromIntent(intent: Intent) = MLBcrCaptureResult().apply {
        number = intent.extras?.getString(INTENT_EXTRA_NAME_BCR_NUMBER)
        expire = intent.extras?.getString(INTENT_EXTRA_NAME_BCR_EXPIRE)
        issuer = intent.extras?.getString(INTENT_EXTRA_NAME_BCR_ISSUER)
        organization = intent.extras?.getString(INTENT_EXTRA_NAME_BCR_ORGANIZATION)
        type = intent.extras?.getString(INTENT_EXTRA_NAME_BCR_TYPE)

        FileInputStream(getLastScannedBankCardFile(context)).use {
            originalBitmap = BitmapFactory.decodeStream(it)
        }
    }

    fun handleActivityResult(requestCode: Int, data: Intent?, fnSuccess: (BankCard) -> Unit): Boolean {
        if (requestCode == RESULT_REQUEST_CODE) {
            val bankCard = data?.getParcelableExtra<Parcelable>(INTENT_RESULT_KEY_NAME) as BankCard?
            bankCard?.let(fnSuccess)
            if (bankCard != null) return true
        }

        return false
    }

    fun setResultAndFinishActivity(activity: Activity, card: BankCard) = activity.run {
        setResult(RESULT_OK, Intent().apply { putExtra(INTENT_RESULT_KEY_NAME, card) })
        finish()
    }

}