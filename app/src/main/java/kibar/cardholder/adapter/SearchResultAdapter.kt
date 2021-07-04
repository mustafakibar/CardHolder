package kibar.cardholder.adapter

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kibar.cardholder.databinding.SearchResultItemRowBinding
import kibar.cardholder.model.SearchResult

class SearchResultAdapter(private val data: Array<SearchResult>) :
    ListAdapter<SearchResult, SearchResultAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val view: SearchResultItemRowBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SearchResultItemRowBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            ).apply {
                root.setOnClickListener {
                    val url = url.text.toString()
                    if (url.isNotBlank()) {
                        try {
                            ContextCompat.startActivity(
                                it.context,
                                Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                                null
                            )
                        } catch (_ignored: Exception) {
                        }
                    }
                }
            }
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) = with(viewHolder.view) {
        val searchResult = data[position]
        title.text = searchResult.title
        content.text = Html.fromHtml(searchResult.content, Html.FROM_HTML_MODE_COMPACT)
        url.text = searchResult.url
    }

    override fun getItemCount() = data.size

    class DiffCallback : DiffUtil.ItemCallback<SearchResult>() {

        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult) =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult) =
            oldItem.title == newItem.title && oldItem.content == newItem.content
    }

}