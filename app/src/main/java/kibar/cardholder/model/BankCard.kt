package kibar.cardholder.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kibar.cardholder.utils.getCurrentDateTime
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "bank_card")
@Parcelize
class BankCard(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val number: String,
    val expire: String,
    val issuer: String?,
    val organization: String?,
    override val createdAt: Date = getCurrentDateTime()
) : Card, Parcelable {
    override val id: String
        get() = name
}