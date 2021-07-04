package kibar.cardholder.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kibar.cardholder.data.bankcard.BankCardDao
import kibar.cardholder.model.BankCard
import java.util.*

@Database(entities = [BankCard::class], version = 1, exportSchema = false)
@TypeConverters(AppDatabase.DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val bankCardDao: BankCardDao

    companion object {
        const val DB_NAME = "cardholder.db"
    }

    object DateConverter {
        @TypeConverter
        fun toDate(date: Long): Date {
            return Date(date)
        }

        @TypeConverter
        fun fromDate(date: Date?): Long {
            return date?.time ?: Calendar.getInstance().timeInMillis
        }
    }

}