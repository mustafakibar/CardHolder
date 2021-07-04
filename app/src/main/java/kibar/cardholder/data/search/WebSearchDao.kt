package kibar.cardholder.data.search

import androidx.room.Dao
import kibar.cardholder.model.SearchResult

@Dao
interface WebSearchDao {

    suspend fun search(text: String): List<SearchResult>

}

