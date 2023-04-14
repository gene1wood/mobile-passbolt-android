package com.passbolt.mobile.android.feature.main.mainscreen

import com.passbolt.mobile.android.core.fulldatarefresh.DataRefreshStatus.Finished
import com.passbolt.mobile.android.core.fulldatarefresh.FullDataRefreshExecutor
import com.passbolt.mobile.android.core.inappreview.InAppReviewInteractor
import com.passbolt.mobile.android.core.mvp.authentication.BaseAuthenticatedContract
import com.passbolt.mobile.android.core.mvp.authentication.BaseAuthenticatedPresenter
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.core.resourcetypes.ResourceTypeFactory
import com.passbolt.mobile.android.database.impl.resources.GetIsResourceTypeSupportedUseCase
import com.passbolt.mobile.android.feature.main.mainscreen.bottomnavigation.MainBottomNavigationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainPresenter(
    private val inAppReviewInteractor: InAppReviewInteractor,
    private val fullDataRefreshExecutor: FullDataRefreshExecutor,
    private val getIsResourceTypeSupportedUseCase: GetIsResourceTypeSupportedUseCase,
    coroutineLaunchContext: CoroutineLaunchContext
) : BaseAuthenticatedPresenter<MainContract.View>(coroutineLaunchContext), MainContract.Presenter {

    override var view: MainContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + coroutineLaunchContext.ui)

    override fun attach(view: MainContract.View) {
        super<BaseAuthenticatedPresenter>.attach(view)
        setupBottomNavigation()
        performFullDataRefresh()
        view.checkForAppUpdates()
        if (inAppReviewInteractor.shouldShowInAppReviewFlow()) {
            view.tryLaunchReviewFlow()
            inAppReviewInteractor.inAppReviewFlowShowed()
        }
    }

    private fun setupBottomNavigation() {
        scope.launch {
            val isStandaloneTotpSupported = getIsResourceTypeSupportedUseCase.execute(
                GetIsResourceTypeSupportedUseCase.Input(ResourceTypeFactory.SLUG_TOTP)
            ).isSupported
            view?.setupBottomNavigation(MainBottomNavigationModel(isOtpTabVisible = isStandaloneTotpSupported))
        }
    }

    override fun performFullDataRefresh() {
        fullDataRefreshExecutor.attach(this@MainPresenter as BaseAuthenticatedPresenter<BaseAuthenticatedContract.View>)
        fullDataRefreshExecutor.performFullDataRefresh()
        scope.launch {
            fullDataRefreshExecutor.dataRefreshStatusFlow.collectLatest {
                if (it is Finished) setupBottomNavigation()
            }
        }
    }

    override fun appUpdateDownloaded() {
        view?.showAppUpdateDownloadedSnackbar()
    }

    override fun detach() {
        fullDataRefreshExecutor.detach()
        scope.coroutineContext.cancelChildren()
        super<BaseAuthenticatedPresenter>.detach()
    }
}
