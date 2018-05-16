/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.manager

import com.toshi.crypto.HDWallet
import com.toshi.crypto.util.TypeConverter
import com.toshi.extensions.getTimeoutSingle
import com.toshi.manager.ethRegistration.EthGcmRegistration
import com.toshi.manager.network.CurrencyInterface
import com.toshi.manager.network.CurrencyService
import com.toshi.manager.network.EthereumService
import com.toshi.manager.network.EthereumServiceInterface
import com.toshi.manager.store.TokenStore
import com.toshi.model.local.network.Network
import com.toshi.model.local.network.Networks
import com.toshi.model.network.Balance
import com.toshi.model.network.Currencies
import com.toshi.model.network.ExchangeRate
import com.toshi.model.network.token.CustomERCToken
import com.toshi.model.network.token.ERC20Token
import com.toshi.model.network.token.ERC721TokenWrapper
import com.toshi.model.network.token.ERC721Tokens
import com.toshi.model.sofa.payment.Payment
import com.toshi.util.CurrencyUtil
import com.toshi.util.EthUtil
import com.toshi.util.EthUtil.BIG_DECIMAL_SCALE
import com.toshi.util.logging.LogUtil
import com.toshi.util.sharedPrefs.AppPrefs
import com.toshi.util.sharedPrefs.AppPrefsInterface
import com.toshi.util.sharedPrefs.BalancePrefs
import com.toshi.util.sharedPrefs.BalancePrefsInterface
import com.toshi.view.BaseApplication
import rx.Completable
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import java.math.RoundingMode

class BalanceManager(
        private val ethService: EthereumServiceInterface = EthereumService,
        private val currencyService: CurrencyInterface = CurrencyService.getApi(),
        private val balancePrefs: BalancePrefsInterface = BalancePrefs(),
        private val appPrefs: AppPrefsInterface = AppPrefs,
        private val baseApplication: BaseApplication = BaseApplication.get(),
        private val walletObservable: Observable<HDWallet>,
        private val ethGcmRegistration: EthGcmRegistration = EthGcmRegistration(
                ethService = ethService,
                walletObservable = walletObservable
        ),
        private val tokenStore: TokenStore = TokenStore(baseApplication),
        private val networks: Networks = Networks.getInstance(),
        private val scheduler: Scheduler = Schedulers.io()
) {
    private var connectivitySub: Subscription? = null
    val balanceObservable: BehaviorSubject<Balance> = BehaviorSubject.create<Balance>()

    fun init(): Completable {
        initCachedBalance()
        return registerEthGcm()
                .onErrorComplete()
                .doOnCompleted { attachConnectivityObserver() }
    }

    private fun initCachedBalance() {
        readLastKnownBalance()
                .map { Balance(it) }
                .flatMapCompletable { handleNewBalance(it) }
                .subscribe(
                        {},
                        { LogUtil.exception("Error while reading last known balance $it") }
                )
    }

    private fun attachConnectivityObserver() {
        clearConnectivitySubscription()
        connectivitySub = baseApplication
                .isConnectedSubject
                .subscribeOn(scheduler)
                .filter { isConnected -> isConnected }
                .subscribe(
                        { handleConnectivity() },
                        { LogUtil.exception("Error checking connection state", it) }
                )
    }

    private fun handleConnectivity() {
        refreshBalance()
        registerEthGcm()
                .subscribeOn(scheduler)
                .subscribe(
                        { },
                        { LogUtil.exception("Error while registering eth gcm", it) }
                )
    }

    fun registerEthGcm(): Completable = ethGcmRegistration.forceRegisterEthGcm()

    fun refreshBalance() {
        getBalance()
                .observeOn(scheduler)
                .flatMapCompletable { handleNewBalance(it) }
                .doOnError { updateBalanceWithLastKnownBalance() }
                .subscribe(
                        {},
                        { LogUtil.exception("Error while fetching balance", it) }
                )
    }

    private fun updateBalanceWithLastKnownBalance() {
        readLastKnownBalance()
                .map { Balance(it) }
                .subscribe(
                        { balanceObservable.onNext(it) },
                        { LogUtil.exception("Error while updating balance with last known balance", it) }
                )
    }

    private fun getBalance(): Single<Balance> {
        return getWallet()
                .flatMap { ethService.get().getBalance(it.paymentAddress) }
                .subscribeOn(scheduler)
    }

    fun getERC20Tokens(): Single<List<ERC20Token>> {
        return getWallet()
                .flatMap { getERC20Tokens(it.getCurrentWalletIndex(), networks.currentNetwork.id) }
    }

    private fun getERC20Tokens(walletIndex: Int, networkId: String): Single<List<ERC20Token>> {
        return Single.concat(
                tokenStore.getAllTokens(networkId = networkId, walletIndex = walletIndex),
                fetchERC20TokensFromNetwork()
        )
        .first { isTokensFresh(it) }
        .toSingle()
    }

    private fun fetchERC20TokensFromNetwork(): Single<List<ERC20Token>> {
        return getWallet()
                .flatMap { ethService.get().getTokens(it.paymentAddress) }
                .map { it.tokens }
                .flatMap { saveERC20Tokens(it) }
                .subscribeOn(scheduler)
    }

    private fun saveERC20Tokens(ERC20Tokens: List<ERC20Token>): Single<List<ERC20Token>> {
        return getWallet()
                .flatMap { saveERC20Tokens(ERC20Tokens, it.getCurrentWalletIndex(), networks.currentNetwork.id) }
    }

    private fun saveERC20Tokens(ERC20Tokens: List<ERC20Token>, walletIndex: Int, networkId: String): Single<List<ERC20Token>> {
        return tokenStore.saveAllTokens(ERC20Tokens, networkId, walletIndex)
    }

    private fun isTokensFresh(ERC20Tokens: List<ERC20Token>): Boolean {
        return when {
            ERC20Tokens.isEmpty() -> false
            !baseApplication.isConnected -> true
            else -> !ERC20Tokens[0].needsRefresh()
        }
    }

    fun getERC20Token(contractAddress: String): Single<ERC20Token> {
        return getWallet()
                .flatMap { ethService.get().getToken(it.paymentAddress, contractAddress) }
                .subscribeOn(scheduler)
    }

    fun getERC721Tokens(): Single<ERC721Tokens> {
        return getWallet()
                .flatMap { ethService.get().getCollectibles(it.paymentAddress) }
                .subscribeOn(scheduler)
    }

    fun getERC721Token(contactAddress: String): Single<ERC721TokenWrapper> {
        return getWallet()
                .flatMap { ethService.get().getCollectible(it.paymentAddress, contactAddress) }
                .subscribeOn(scheduler)
    }

    fun addCustomToken(customERCToken: CustomERCToken): Completable {
        return ethService
                .get()
                .timestamp
                .flatMapCompletable { ethService.get().addCustomToken(it.get(), customERCToken) }
                .subscribeOn(scheduler)
    }

    private fun getWallet(): Single<HDWallet> {
        return walletObservable
                .getTimeoutSingle()
                .subscribeOn(scheduler)
    }

    private fun handleNewBalance(balance: Balance): Completable {
        return writeLastKnownBalance(balance)
                .doOnCompleted { balanceObservable.onNext(balance) }
    }

    fun generateLocalPrice(payment: Payment): Single<Payment> {
        val weiAmount = TypeConverter.StringHexToBigInteger(payment.value)
        val ethAmount = EthUtil.weiToEth(weiAmount)
        return getLocalCurrencyExchangeRate()
                .map { toLocalCurrencyString(it, ethAmount) }
                .map(payment::setLocalPrice)
    }

    fun getLocalCurrencyExchangeRate(): Single<ExchangeRate> {
        return getLocalCurrency()
                .flatMap { fetchLatestExchangeRate(it) }
                .subscribeOn(scheduler)
    }

    private fun fetchLatestExchangeRate(code: String): Single<ExchangeRate> = currencyService.getRates(code)

    fun getCurrencies(): Single<Currencies> {
        return currencyService
                .currencies
                .subscribeOn(scheduler)
    }

    fun convertEthToLocalCurrencyString(ethAmount: BigDecimal): Single<String> {
        return getLocalCurrencyExchangeRate()
                .map { toLocalCurrencyString(it, ethAmount) }
    }

    fun toLocalCurrencyString(exchangeRate: ExchangeRate, ethAmount: BigDecimal): String {
        val marketRate = exchangeRate.rate
        val localAmount = marketRate.multiply(ethAmount)

        val numberFormat = CurrencyUtil.getNumberFormat()
        numberFormat.isGroupingUsed = true
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2

        val amount = numberFormat.format(localAmount)
        val currencyCode = CurrencyUtil.getCode(exchangeRate.to)
        val currencySymbol = CurrencyUtil.getSymbol(exchangeRate.to)

        return String.format("%s%s %s", currencySymbol, amount, currencyCode)
    }

    private fun getLocalCurrency(): Single<String> = Single.fromCallable { appPrefs.getCurrency() }

    fun convertLocalCurrencyToEth(localAmount: BigDecimal): Single<BigDecimal> {
        return getLocalCurrencyExchangeRate()
                .flatMap { mapToEth(it, localAmount) }
    }

    private fun mapToEth(exchangeRate: ExchangeRate,
                         localAmount: BigDecimal): Single<BigDecimal> {
        return Single.fromCallable {
            if (localAmount.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO
            val marketRate = exchangeRate.rate
            if (marketRate.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO
            return@fromCallable localAmount.divide(marketRate, BIG_DECIMAL_SCALE, RoundingMode.HALF_DOWN)
        }
    }

    fun getTransactionStatus(transactionHash: String): Single<Payment> {
        return EthereumService.getStatusOfTransaction(transactionHash)
    }

    private fun readLastKnownBalance(): Single<String> {
        return walletObservable.getTimeoutSingle()
                .subscribeOn(scheduler)
                .map { balancePrefs.readLastKnownBalance(it.getCurrentWalletIndex()) }
    }

    private fun writeLastKnownBalance(balance: Balance): Completable {
        return walletObservable.getTimeoutSingle()
                .subscribeOn(scheduler)
                .doOnSuccess { balancePrefs.writeLastKnownBalance(it.getCurrentWalletIndex(), balance) }
                .toCompletable()
    }

    fun changeNetwork(network: Network) = ethGcmRegistration.changeNetwork(network)

    fun unregisterFromEthGcm(token: String) = ethGcmRegistration.unregisterFromEthGcm(token)

    fun clear() {
        clearConnectivitySubscription()
        balancePrefs.clear()
        ethGcmRegistration.clear()
    }

    private fun clearConnectivitySubscription() = connectivitySub?.unsubscribe()
}