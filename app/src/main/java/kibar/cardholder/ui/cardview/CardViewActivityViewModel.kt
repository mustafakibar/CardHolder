package kibar.cardholder.ui.cardview

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val bankCardRepository: BankCardRepository
) :
    ViewModel() {

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
        if (name.isNullOrBlank() || name.length <= 6) {
            return ValidationResult.Err(
                ErrType.NAME,
                "Lütfen kartınıza en az 6 karakterden oluşan bir isim veriniz"
            )
        }

        val isFound = bankCardRepository.findByName(name)
        if (isFound != null) {
            return ValidationResult.Err(
                ErrType.NAME,
                "Girmiş olduğunuz '$name' kart isminde başka bir kartınız mevcut"
            )
        }

        return when {
            number.isNullOrBlank() -> ValidationResult.Err(
                ErrType.NUMBER,
                "Lütfen geçerli bir kart numarası giriniz"
            )
            expire.isNullOrBlank() -> ValidationResult.Err(
                ErrType.EXPIRE,
                "Lütfen son kullanım tarihini ay/yıl olarak giriniz"
            )
            else -> ValidationResult.Ok(name, number, expire, issuer, organization)
        }
    }

}