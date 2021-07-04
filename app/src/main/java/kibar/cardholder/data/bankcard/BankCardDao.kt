package kibar.cardholder.data.bankcard

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kibar.cardholder.model.BankCard

@Dao
interface BankCardDao {

    @Query("SELECT * FROM bank_card ORDER BY createdAt DESC")
    fun getAll(): LiveData<List<BankCard>>

    @Query("SELECT * FROM bank_card WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): BankCard?

    @Insert
    fun insertAll(vararg cards: BankCard)

    @Delete
    fun delete(card: BankCard)

    @Query("SELECT COUNT(*) FROM bank_card")
    fun count(): Int

}

