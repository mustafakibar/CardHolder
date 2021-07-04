package kibar.cardholder.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kibar.cardholder.data.bankcard.BankCardRepository
import kibar.cardholder.data.search.WebSearchRepository
import kibar.cardholder.model.BankCard
import kibar.cardholder.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val bankCardRepository: BankCardRepository,
    private val searchRepository: WebSearchRepository
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

    fun addBankCard(bankCard: BankCard) {
        bankCardRepository.insertAll(bankCard)
    }

    fun removeBankCard(bankCard: BankCard) {
        bankCardRepository.delete(bankCard)
    }

    fun totalBankCard() = bankCardRepository.count()

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