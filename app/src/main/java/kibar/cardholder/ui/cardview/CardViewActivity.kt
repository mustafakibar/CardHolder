package kibar.cardholder.ui.cardview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kibar.cardholder.R
import kibar.cardholder.databinding.ActivityCardViewBinding
import kibar.cardholder.model.BankCard
import kibar.cardholder.ui.BaseActivity
import kibar.cardholder.ui.cardview.CardViewActivityViewModel.BinData.*
import kibar.cardholder.ui.cardview.CardViewActivityViewModel.ValidationResult
import kibar.cardholder.utils.toast
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardViewActivity : BaseActivity() {

    private lateinit var binding: ActivityCardViewBinding
    private val viewModel: CardViewActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityCardViewBinding.inflate(layoutInflater).apply {
                val toolbar = includedAppbar.toolbar
                setSupportActionBar(toolbar)
                supportActionBar?.title = "Kart Görüntüle & Düzenle"
                setContentView(root)
            }

        viewModel.parseMLBcrCaptureResultFromIntent(intent)
        viewModel.bcrCaptureResult.observe(this) { result ->
            binding.cardImage.setImageBitmap(result.originalBitmap)
            binding.cardDetails.cardNumber.setText(result.number)
            binding.cardDetails.cardExpire.setText(result.expire)
            binding.cardDetails.cardIssuer.setText(result.issuer) //banka ismi
            binding.cardDetails.cardOrganization.setText(result.organization) //visa, mastercard etc.

            if (result.number.isNotBlank()) lifecycleScope.launch { viewModel.loadBin(result.number) }
        }

        fun cardDetailsLayoutAnimation(enable: Boolean) {
            binding.cardDetails.root.clearAnimation()
            binding.cardDetails.root
                .animate()
                .setInterpolator(AnticipateOvershootInterpolator(1.3f))
                .alpha(if (enable) 0.1f else 1f)
                .setDuration(500)
                .start()
        }

        viewModel.binData.observe(this) { binData ->
            val loading = binData is Loading
            binding.progressbar.visibility =
                if (loading) View.VISIBLE else View.GONE

            cardDetailsLayoutAnimation(enable = loading)

            when (binData) {
                Loading -> {
                    //ignored
                }
                is Success<*> -> {
                    binData.data.bank?.name?.let { binding.cardDetails.cardIssuer.setText(it) }
                    binData.data.brand?.let { binding.cardDetails.cardOrganization.setText(it) }
                }
                is Fail -> toast(
                    binData.err.message ?: "Kart numarasına ait banka bilgilerine ulaşılamadı!"
                )
            }
        }
    }

    private fun acceptAndClose() = lifecycleScope.launch {
        when (val result = viewModel.validate(
            name = binding.cardDetails.cardName.text?.toString()?.trim(),
            number = binding.cardDetails.cardNumber.text?.toString()?.trim()
                ?.replace("\\s+".toRegex(), " "),
            expire = binding.cardDetails.cardExpire.text?.toString()?.trim()
                ?.replace("\\s+".toRegex(), " "),
            issuer = binding.cardDetails.cardIssuer.text?.toString()?.trim(),
            organization = binding.cardDetails.cardOrganization.text?.toString()?.trim()
        )) {
            is ValidationResult.Err -> {
                when (result.type) {
                    CardViewActivityViewModel.ErrType.NAME -> binding.cardDetails.cardName.requestFocus()
                    CardViewActivityViewModel.ErrType.EXPIRE -> binding.cardDetails.cardExpire.requestFocus()
                    CardViewActivityViewModel.ErrType.NUMBER -> binding.cardDetails.cardNumber.requestFocus()
                }

                toast(result.message)
            }
            is ValidationResult.Ok -> {
                viewModel.setResultAndFinishActivity(
                    activity = this@CardViewActivity,
                    bankCard = BankCard(
                        name = result.name,
                        number = result.number,
                        expire = result.expire,
                        issuer = result.issuer,
                        organization = result.organization
                    )
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_card_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_accept -> acceptAndClose()
        }

        return super.onOptionsItemSelected(item)
    }

}