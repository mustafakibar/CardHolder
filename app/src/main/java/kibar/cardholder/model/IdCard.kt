package kibar.cardholder.model

class IdCard(
    val firstName: String,
    val lastName: String,
    override val id: String, override val createdAt: String
) : Card