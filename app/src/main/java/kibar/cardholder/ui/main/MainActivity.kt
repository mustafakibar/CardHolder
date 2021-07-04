package kibar.cardholder.ui.main

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants
import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult
import com.huawei.hms.mlsdk.asr.MLAsrConstants
import com.huawei.hms.mlsdk.asr.MLAsrListener
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer
import com.jackandphantom.carouselrecyclerview.CarouselLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kibar.cardholder.R
import kibar.cardholder.adapter.CreditCardAdapter
import kibar.cardholder.adapter.OnCreditCardItemDeleted
import kibar.cardholder.adapter.SearchResultAdapter
import kibar.cardholder.databinding.ActivityMainBinding
import kibar.cardholder.ui.BaseActivity
import kibar.cardholder.ui.cardview.CardViewHelper
import kibar.cardholder.ui.main.MainActivityViewModel.BankCardSearchResult
import kibar.cardholder.utils.PermissionUtil
import kibar.cardholder.utils.set
import kibar.cardholder.utils.toast
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    companion object {
        private const val ASR_REQUEST = 300
        private const val KEY_NAME_INSTANCE_STATE_PERM_DATA = "STATE_PERM_DATA"
    }

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var bcrCapture: MLBcrCapture

    @Inject
    lateinit var cardViewHelper: CardViewHelper

    @Inject
    lateinit var asrRecognizer: MLAsrRecognizer

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var permData = mutableMapOf<Int, Boolean>()

    private val asrListener by lazy {
        object : MLAsrListener {
            override fun onResults(result: Bundle?) {}
            override fun onRecognizingResults(partialResult: Bundle?) {}
            override fun onError(errorCode: Int, errorMessage: String?) {}
            override fun onStartListening() {}
            override fun onStartingOfSpeech() {}
            override fun onVoiceDataReceived(p0: ByteArray?, p1: Float, p2: Bundle?) {}
            override fun onState(p0: Int, p1: Bundle?) {}
        }
    }

    private val bcrListener by lazy {
        object : MLBcrCapture.Callback {
            override fun onSuccess(result: MLBcrCaptureResult) {
                cardViewHelper.startActivityForResult(this@MainActivity, result)
            }

            override fun onFailure(recCode: Int, bitmap: Bitmap) {
                toast(getString(R.string.unableToScanCardWithErrorCode, recCode))
            }

            override fun onCanceled() {
                toast(getString(R.string.cardScanCanceled))
            }

            override fun onDenied() {
                toast(getString(R.string.cardScanNotSupportedByCamera))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            val toolbar = includedAppbar.toolbar
            setSupportActionBar(toolbar)
            supportActionBar?.title = getString(R.string.yourCards)
            setContentView(root)
        }

        lifecycleScope.launchWhenStarted {
            binding.recyclerBankCard.apply {
                set3DItem(false)
                setInfinite(true)
                setAlpha(true)
                setIntervalRatio(0.85f)
            }

            fun performBankCardItemSelection(position: Int) {
                val selectedCard = viewModel.bankCards.value?.get(position)
                if (selectedCard != null) {
                    binding.cardDetailsHeaderTitle.text =
                        if (selectedCard.issuer?.isNotBlank() == true)
                            getString(R.string.mainBankNameTitle, selectedCard.issuer)
                        else getString(R.string.noBankName)

                    lifecycleScope.launch { viewModel.handleOnCardSelected(selectedCard) }
                }
            }

            binding.recyclerBankCard.setItemSelectListener(object :
                CarouselLayoutManager.OnSelected {
                override fun onItemSelected(position: Int) {
                    performBankCardItemSelection(position)
                }
            })

            val bankCardItemDeleteListener: OnCreditCardItemDeleted =
                { viewModel.removeBankCard(it) }

            viewModel.bankCards.observe(this@MainActivity, { bankCards ->
                binding.recyclerBankCard.adapter =
                    CreditCardAdapter(
                        bankCards.toTypedArray(),
                        itemDeleteListener = bankCardItemDeleteListener
                    )

                val size = bankCards.size
                if (size > 0) {
                    binding.warnForAddNewCard.visibility = View.GONE
                    binding.cardDetailsAndSearchResults.visibility = View.VISIBLE
                    performBankCardItemSelection(0)
                } else {
                    binding.warnForAddNewCard.visibility = View.VISIBLE
                    binding.cardDetailsAndSearchResults.visibility = View.GONE
                    binding.recyclerSearchResult.adapter = null
                }

                supportActionBar?.title =
                    if (size == 0) getString(R.string.noCardFounds)
                    else getString(R.string.totalCards, size)
            })

            binding.recyclerSearchResult.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(applicationContext)
                itemAnimator = DefaultItemAnimator()
            }

            fun cardDetailsAndSearchResultsLayoutAnimation(enable: Boolean) {
                binding.cardDetailsAndSearchResults.clearAnimation()
                binding.cardDetailsAndSearchResults
                    .animate()
                    .setInterpolator(AnticipateOvershootInterpolator(1.3f))
                    .alpha(if (enable) 0.1f else 1f)
                    .setDuration(500)
                    .start()
            }

            var itemTouch = true
            binding.recyclerBankCard.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(r: RecyclerView, e: MotionEvent) = itemTouch
            })

            viewModel.selectedBankCardWebSearchResult.observe(this@MainActivity) { bankCardWithSearchResult ->
                val isLoading = bankCardWithSearchResult is BankCardSearchResult.Loading
                itemTouch = isLoading
                binding.progressbar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
                cardDetailsAndSearchResultsLayoutAnimation(isLoading)

                if (isLoading) {
                    // done
                    return@observe
                }

                when (bankCardWithSearchResult) {
                    is BankCardSearchResult.Fail -> {
                        toast(
                            bankCardWithSearchResult.err.message
                                ?: getString(R.string.unableToFetchCampaignInformationOfYourBank)
                        )
                    }
                    BankCardSearchResult.Loading -> {
                        //ignored
                    }
                    is BankCardSearchResult.Success -> {
                        if (viewModel.lastSelectedCard.value?.equals(bankCardWithSearchResult.bankCard) == true
                        ) {
                            binding.recyclerSearchResult.adapter =
                                SearchResultAdapter(bankCardWithSearchResult.results.toTypedArray())
                        }
                    }
                }
            }

            viewModel.initML(this@MainActivity)

            PermissionUtil.Camera.requestPermission { PermissionUtil.ReadExternalStorage.requestPermission() }
        }

        super.onCreate(savedInstanceState)
    }

    private fun startCapture() {
        PermissionUtil.Camera.requestPermission { PermissionUtil.ReadExternalStorage.requestPermission() }

        bcrCapture.captureFrame(this, this.bcrListener)
    }

    // todo
    private fun startRecognizing() {
        PermissionUtil.RecordAudio.requestPermission() {
            asrRecognizer.setAsrListener(asrListener)
            startActivityForResult(
                this, Intent(this, MLAsrCaptureActivity::class.java)
                    .putExtra(MLAsrCaptureConstants.LANGUAGE, MLAsrConstants.LAN_EN_US)
                    .putExtra(
                        MLAsrCaptureConstants.FEATURE,
                        MLAsrCaptureConstants.FEATURE_WORDFLUX
                    ), ASR_REQUEST, null
            )
        }
    }

    private fun PermissionUtil.requestPermission(chainFn: () -> Unit = {}) {
        if (checkSelfPermission(applicationContext, name) == PERMISSION_GRANTED) {
            permData[requestCode] = true
            chainFn()
            return
        }

        if (shouldShowRequestPermissionRationale(this@MainActivity, name)) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(READ_EXTERNAL_STORAGE),
                requestCode
            )
        } else {
            if (sharedPreferences.getBoolean(storageKeyName, false)) {
                AlertDialog.Builder(this@MainActivity)
                    .apply {
                        title = askUserDialogTitle
                        setMessage(askUserDialogMessage)
                        setNegativeButton(getString(R.string.cancel)) { _, _ -> toast(getString(R.string.accessDenied)) }
                        setPositiveButton(getString(R.string.openAppSettings)) { _, _ ->
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${this@MainActivity.packageName}")
                                )
                            )
                        }
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, permsArray, requestCode)
                sharedPreferences[storageKeyName] = true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        PermissionUtil.createFromRequestCode(requestCode)
            ?.let {
                permData[requestCode] =
                    grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED
            }
            ?: Timber.d("Unsupported request code '$requestCode' for '${permissions.joinToString()} permission(s)'")

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> startCapture()
            R.id.menu_mic -> startRecognizing()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_NAME_INSTANCE_STATE_PERM_DATA, Gson().toJson(permData))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState.getString(KEY_NAME_INSTANCE_STATE_PERM_DATA)?.let { jsonData ->
            try {
                permData = Gson().fromJson(
                    jsonData,
                    object : TypeToken<Map<Int, Boolean>>() {}.type
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val handled = cardViewHelper.handleActivityResult(
            requestCode = requestCode,
            data = data
        ) { bankCard -> viewModel.addBankCard(bankCard) }

        if (handled) return

        if (requestCode == ASR_REQUEST && resultCode == MLAsrCaptureConstants.ASR_SUCCESS) {
            // todo
        }
    }

}
