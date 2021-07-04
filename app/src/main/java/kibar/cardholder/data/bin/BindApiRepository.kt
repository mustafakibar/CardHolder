package kibar.cardholder.data.bin

import kibar.cardholder.model.BinResult
import kotlinx.coroutines.flow.Flow

interface BindApiRepository {
    fun getBin(number: String): Flow<BinResult>
}

