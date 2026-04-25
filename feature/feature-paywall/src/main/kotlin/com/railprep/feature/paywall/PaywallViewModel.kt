package com.railprep.feature.paywall

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val MONTHLY_ID = "rrb_ntpc_pro_monthly"
private const val QUARTERLY_ID = "rrb_ntpc_pro_quarterly"

data class PaywallPlan(
    val productId: String,
    val title: String,
    val price: String,
    val available: Boolean,
)

data class PaywallUiState(
    val loading: Boolean = true,
    val plans: List<PaywallPlan> = fallbackPlans(),
    val owned: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val productDetailsById = mutableMapOf<String, ProductDetails>()
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(::onPurchasesUpdated)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    private val _state = MutableStateFlow(PaywallUiState())
    val state: StateFlow<PaywallUiState> = _state.asStateFlow()

    init { connectAndLoad() }

    fun refresh() = connectAndLoad()

    fun buy(activity: Activity, productId: String) {
        val details = productDetailsById[productId]
        if (details == null) {
            _state.update { it.copy(message = "unavailable") }
            return
        }
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken.isNullOrBlank()) {
            _state.update { it.copy(message = "unavailable") }
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.update { it.copy(message = "launch_failed") }
        }
    }

    override fun onCleared() {
        if (billingClient.isReady) billingClient.endConnection()
        super.onCleared()
    }

    private fun connectAndLoad() {
        if (billingClient.isReady) {
            queryProducts()
            queryPurchases()
            return
        }
        _state.update { it.copy(loading = true, message = null) }
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryProducts()
                        queryPurchases()
                    } else {
                        _state.update { it.copy(loading = false, message = "setup_failed") }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _state.update { it.copy(loading = false, message = "disconnected") }
                }
            },
        )
    }

    private fun queryProducts() {
        val productList = listOf(MONTHLY_ID, QUARTERLY_ID).map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _state.update { it.copy(loading = false, plans = fallbackPlans(), message = "query_failed") }
                return@queryProductDetailsAsync
            }
            productDetailsById.clear()
            val plans = detailsResult.productDetailsList.map { details ->
                productDetailsById[details.productId] = details
                PaywallPlan(
                    productId = details.productId,
                    title = displayTitle(details.productId),
                    price = displayPrice(details),
                    available = true,
                )
            }
            _state.update {
                it.copy(
                    loading = false,
                    plans = if (plans.isEmpty()) fallbackPlans() else plans.sortedBy { plan -> plan.productId },
                    message = if (plans.isEmpty()) "unavailable" else null,
                )
            }
        }
    }

    private fun queryPurchases() {
        val params = com.android.billingclient.api.QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> handlePurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> _state.update { it.copy(message = "cancelled") }
            else -> _state.update { it.copy(message = "purchase_failed") }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val owned = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { it == MONTHLY_ID || it == QUARTERLY_ID }
        }
        purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach { purchase ->
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { /* Play backend is source of truth. */ }
            }
        _state.update {
            it.copy(
                owned = owned,
                message = if (owned) "owned" else it.message,
            )
        }
    }

    private fun displayPrice(details: ProductDetails): String {
        val offer = details.subscriptionOfferDetails?.firstOrNull()
        val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        return phase?.formattedPrice ?: fallbackPlans().first { it.productId == details.productId }.price
    }

    private fun displayTitle(productId: String): String = when (productId) {
        MONTHLY_ID -> "Monthly"
        QUARTERLY_ID -> "Quarterly"
        else -> productId
    }
}

private fun fallbackPlans(): List<PaywallPlan> = listOf(
    PaywallPlan(MONTHLY_ID, "Monthly", "₹79/month", available = false),
    PaywallPlan(QUARTERLY_ID, "Quarterly", "₹199/quarter", available = false),
)
