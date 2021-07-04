package kibar.cardholder.data.bankcard

import kibar.cardholder.model.BankCard
import javax.inject.Inject

class BankCardRepository @Inject constructor(private val dao: BankCardDao) {

    fun getAll() = dao.getAll()
    fun findByName(name: String) = dao.findByName(name)
    fun insertAll(vararg cards: BankCard) = dao.insertAll(*cards)
    fun delete(card: BankCard) = dao.delete(card)
    fun count() = dao.count()

}