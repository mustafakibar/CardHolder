package kibar.cardholder.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kibar.cardholder.utils.getCurrentDateTimeAsFormatted
import kotlinx.parcelize.Parcelize

@Entity(tableName = "bank_card")
@Parcelize
class BankCard(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val number: String,
    val expire: String,
    val issuer: String?,
    val organization: String?,
    override val createdAt: String = getCurrentDateTimeAsFormatted()
) : Card, Parcelable {
    override val id: String
        get() = name
}