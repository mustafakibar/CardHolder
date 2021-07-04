package kibar.cardholder.ui.cardview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kibar.cardholder.R
import kibar.cardholder.data.bankcard.BankCardRepository
import kibar.cardholder.data.bin.BindApiRepository
import kibar.cardholder.model.BankCard
import kibar.cardholder.model.BinResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class CardViewActivityViewModel @Inject constructor(
    private val bindApiRepository: BindApiRepository,
    private val bankCardRepository: BankCardRepository,
    @field:SuppressLint("StaticFieldLeak")
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val MIN_REQ_NAME_LENGTH = 3
    }

    sealed class BinData {
        class Success<T : BinResult>(val data: T) : BinData()
        class Fail(val err: Throwable) : BinData()
        object Loading : BinData()
    }

    sealed class ErrType {
        object NAME : ErrType()
        object NUMBER : ErrType()
        object EXPIRE : ErrType()
    }

    sealed class ValidationResult {
        class Ok(
            val name: String,
            val number: String,
            val expire: String,
            val issuer: String?,
            val organization: String?
        ) : ValidationResult()

        class Err(val type: ErrType, val message: String) : ValidationResult()
    }

    @Inject
    lateinit var cardViewHelper: CardViewHelper

    val binData = MutableLiveData<BinData>()
    val bcrCaptureResult = MutableLiveData<MLBcrCaptureResult>()

    fun parseMLBcrCaptureResultFromIntent(intent: Intent) {
        bcrCaptureResult.postValue(cardViewHelper.createMLBcrCaptureResultFromIntent(intent))
    }

    suspend fun loadBin(cardNumber: String) = bindApiRepository.getBin(cardNumber)
        .onStart { binData.postValue(BinData.Loading) }
        .catch { binData.postValue(BinData.Fail(it)) }
        .collect {
            delay(500) // for smooth alpha animation
            binData.postValue(BinData.Success(it))
        }

    fun setResultAndFinishActivity(activity: Activity, bankCard: BankCard) {
        cardViewHelper.setResultAndFinishActivity(activity, bankCard)
    }

    fun validate(
        name: String?,
        number: String?,
        expire: String?,
        issuer: String?,
        organization: String?
    ): ValidationResult {
        if (name.isNullOrBlank() || name.length < MIN_REQ_NAME_LENGTH) {
            return ValidationResult.Err(
                type = ErrType.NAME,
                message = context.getString(R.string.enterCardNameWithMinReq, MIN_REQ_NAME_LENGTH)
            )
        }

        val isFound = bankCardRepository.findByName(name)
        if (isFound != null) {
            return ValidationResult.Err(
                type = ErrType.NAME,
                message = context.getString(R.string.cardAlreadyExistWithTheNameYouEntered, name)
            )
        }

        return when {
            number.isNullOrBlank() -> ValidationResult.Err(
                type = ErrType.NUMBER,
                message = context.getString(R.string.invalidCardNumber)
            )
            expire.isNullOrBlank() -> ValidationResult.Err(
                type = ErrType.EXPIRE,
                message = context.getString(R.string.invalidExpireDate)
            )
            else -> ValidationResult.Ok(
                name = name,
                number = number,
                expire = expire,
                issuer = issuer,
                organization = organization
            )
        }
    }

}