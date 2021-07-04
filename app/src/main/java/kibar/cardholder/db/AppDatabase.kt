package kibar.cardholder.db

import androidx.room.Database
import androidx.room.RoomDatabase
import kibar.cardholder.data.bankcard.BankCardDao
import kibar.cardholder.model.BankCard

@Database(entities = [BankCard::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val bankCardDao: BankCardDao

    companion object {
        const val DB_NAME = "cardholder.db"
    }

}