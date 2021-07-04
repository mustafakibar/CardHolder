package kibar.cardholder.data.search

import javax.inject.Inject

class WebSearchRepository @Inject constructor(private val dao: WebSearchDao) {

    suspend fun search(text: String) = dao.search(text)

}