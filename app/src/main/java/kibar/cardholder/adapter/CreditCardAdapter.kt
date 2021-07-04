package kibar.cardholder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kibar.cardholder.databinding.CreditCardItemRowBinding
import kibar.cardholder.model.BankCard

typealias OnCreditCardItemDeleted = (BankCard) -> Unit

class CreditCardAdapter(
    private val data: Array<BankCard>,
    private val itemDeleteListener: OnCreditCardItemDeleted? = null
) :
    ListAdapter<BankCard, CreditCardAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val view: CreditCardItemRowBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = CreditCardItemRowBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        val holder = ViewHolder(binding)
        binding.delete.setOnClickListener { itemDeleteListener?.invoke(data[holder.adapterPosition]) }
        return holder
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) =
        with(viewHolder.view.creditCard) {
            val bankCard = data[position]

            cardNumber = bankCard.number
            expiryDate = bankCard.expire

            viewHolder.view.cardName.text = bankCard.name
            viewHolder.view.createdAt.text = bankCard.createdAt

            val organization = bankCard.organization ?: ""
            type = when {
                organization.contains("visa", ignoreCase = true) -> 1
                organization.contains("master", ignoreCase = true) -> 2
                organization.contains("american", ignoreCase = true) -> 3
                organization.contains("discover", ignoreCase = true) -> 4
                else -> 1
            }
        }

    override fun getItemCount() = data.size

    class DiffCallback : DiffUtil.ItemCallback<BankCard>() {

        override fun areItemsTheSame(oldItem: BankCard, newItem: BankCard) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BankCard, newItem: BankCard) =
            oldItem.name == newItem.name
    }

}