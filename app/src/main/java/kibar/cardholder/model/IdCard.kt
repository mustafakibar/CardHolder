package kibar.cardholder.model

import java.util.*

class IdCard(
    val firstName: String,
    val lastName: String,
    override val id: String, override val createdAt: Date
) : Card