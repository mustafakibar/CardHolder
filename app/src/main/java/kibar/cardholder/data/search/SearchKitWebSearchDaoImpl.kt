package kibar.cardholder.data.search

import android.content.Context
import com.huawei.hms.searchkit.SearchKitInstance
import com.huawei.hms.searchkit.bean.WebSearchRequest
import com.huawei.hms.searchkit.utils.Language
import com.huawei.hms.searchkit.utils.Region
import dagger.hilt.android.qualifiers.ApplicationContext
import kibar.cardholder.R
import kibar.cardholder.api.HMSAuthApiService
import kibar.cardholder.model.SearchResult
import kibar.cardholder.utils.isNetworkAvailable
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

typealias SEARCH_QUERY_NAME = String
typealias SEARCH_QUERY_RESULT = List<SearchResult>

class SearchKitWebSearchDaoImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hmsAuthApiService: HMSAuthApiService
) : WebSearchDao {

    private val accessTokenFetchInProgress = AtomicBoolean(false)
    private var accessToken: String = ""
        set(value) {
            SearchKitInstance.getInstance().setInstanceCredential(value)
            field = value
        }

    private val searchRequest by lazy {
        WebSearchRequest().apply {
            setLang(Language.TURKISH)
            setSregion(Region.TURKEY)
            setPs(40)
            setPn(1)
        }
    }

    private val cache = mutableMapOf<SEARCH_QUERY_NAME, SEARCH_QUERY_RESULT>()

    init {
        SearchKitInstance.init(context, context.getString(R.string.clientId))
    }

    private suspend fun checkAccessToken() {
        if (accessToken.isNotBlank() || accessTokenFetchInProgress.get()) {
            return
        }

        accessTokenFetchInProgress.set(true)

        try {
            val token = hmsAuthApiService.requestToken(
                clientId = context.getString(R.string.clientId),
                clientSecret = context.getString(R.string.clientSecret)
            )
            if (token.accessToken.isNullOrBlank()) {
                Timber.e("Unable to retrieve token!")
            }

            accessToken = token.accessToken ?: ""
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        } finally {
            accessTokenFetchInProgress.set(false)
        }
    }

    override suspend fun search(text: String): List<SearchResult> {
        if (!context.isNetworkAvailable()) {
            throw IllegalStateException(context.getString(R.string.unableToFetchCampaignInformationOfYourBankDueToNoInternetConnection))
        }

        checkAccessToken()

        if (cache.containsKey(text)) {
            return cache[text]!!
        }

        if (text.isEmpty()) {
            return Collections.emptyList()
        }

        return SearchKitInstance.getInstance().webSearcher
            .search(searchRequest.apply { setQ(text) })
            .data
            ?.map {
                SearchResult(
                    title = it.title?.trim() ?: "",
                    content = it.snippet?.trim() ?: "",
                    url = it.click_url?.trim() ?: ""
                )
            }
            ?.apply { cache[text] = this }
            ?: Collections.emptyList()
    }

}