package kibar.cardholder.data.bin

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kibar.cardholder.api.BinApiService
import kibar.cardholder.model.BinResult
import kibar.cardholder.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.IllegalStateException
import javax.inject.Inject

class BinApiRepositoryImpl @Inject constructor(
    private val api: BinApiService,
    @ApplicationContext private val context: Context
) : BindApiRepository {

    override fun getBin(number: String): Flow<BinResult> = flow {
        assert(number.length >= 6) { "The card number($number) must be at least 6 digits." }
        if (!context.isNetworkAvailable()) {
            throw IllegalStateException("İnternet bağlantınız olmadığından, kartınıza ait banka bilgileri alınamadı...")
        }

        emit(api.getBin(number.substring(0, if (number.length >= 8) 7 else 5)))
    }.flowOn(Dispatchers.IO)

}