package kibar.cardholder.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kibar.cardholder.R
import kibar.cardholder.api.HMSAuthApiService
import kibar.cardholder.data.bankcard.BankCardRepository
import kibar.cardholder.data.search.WebSearchRepository
import kibar.cardholder.model.BankCard
import kibar.cardholder.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val bankCardRepository: BankCardRepository,
    private val searchRepository: WebSearchRepository,
    private val hmsAuthApiService: HMSAuthApiService,
    private val serviceConfig: AGConnectServicesConfig
) : ViewModel() {

    sealed class BankCardSearchResult {
        object Loading : BankCardSearchResult()
        class Success(val bankCard: BankCard, val results: List<SearchResult>) :
            BankCardSearchResult()

        class Fail(val err: Throwable) : BankCardSearchResult()
    }

    val lastSelectedCard = MutableLiveData<BankCard>()
    val bankCards: LiveData<List<BankCard>> = bankCardRepository.getAll()
    val selectedBankCardWebSearchResult: MutableLiveData<BankCardSearchResult> = MutableLiveData()

    fun initML(context: Context) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                MLApplication.getInstance().apiKey = serviceConfig.getString("client/api_key")
                MLApplication.getInstance()
                    .setAccessToken(hmsAuthApiService.requestToken(
                        clientId = context.getString(R.string.clientId),
                        clientSecret = context.getString(R.string.clientSecret)
                    ).accessToken)
            }
        }
    }

    fun addBankCard(bankCard: BankCard) {
        bankCardRepository.insertAll(bankCard)
    }

    fun removeBankCard(bankCard: BankCard) {
        bankCardRepository.delete(bankCard)
    }

    suspend fun handleOnCardSelected(bankCard: BankCard) {
        lastSelectedCard.postValue(bankCard)
        if (bankCard.issuer != null) {
            if (bankCard.issuer.isBlank()) {
                selectedBankCardWebSearchResult.postValue(
                    BankCardSearchResult.Success(
                        bankCard = bankCard,
                        results = Collections.emptyList()
                    )
                )
                return
            }

            try {
                selectedBankCardWebSearchResult.postValue(BankCardSearchResult.Loading)
                selectedBankCardWebSearchResult.postValue(
                    BankCardSearchResult.Success(
                        bankCard = bankCard,
                        results = withContext(Dispatchers.IO) { searchRepository.search("${bankCard.issuer} kampanya") }
                    )
                )
            } catch (e: Exception) {
                selectedBankCardWebSearchResult.postValue(BankCardSearchResult.Fail(e))
            }
        }
    }

}