package com.passbolt.mobile.android.feature.main.mainscreen

import android.os.Bundle
import androidx.navigation.ui.setupWithNavController
import com.passbolt.mobile.android.common.lifecycleawarelazy.lifecycleAwareLazy
import com.passbolt.mobile.android.core.extension.findNavHostFragment
import com.passbolt.mobile.android.core.security.runtimeauth.RuntimeAuthenticatedFlag
import com.passbolt.mobile.android.feature.authentication.BindingScopedAuthenticatedActivity
import com.passbolt.mobile.android.feature.home.screen.DataRefreshStatus
import com.passbolt.mobile.android.feature.home.screen.HomeDataRefreshExecutor
import com.passbolt.mobile.android.feature.main.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.Flow
import org.koin.android.ext.android.inject

class MainActivity :
    BindingScopedAuthenticatedActivity<ActivityMainBinding, MainContract.View>(ActivityMainBinding::inflate),
    HomeDataRefreshExecutor, MainContract.View {

    override val presenter: MainContract.Presenter by inject()

    private val bottomNavController by lifecycleAwareLazy {
        findNavHostFragment(binding.fragmentContainer.id).navController
    }
    private val runtimeAuthenticatedFlag: RuntimeAuthenticatedFlag by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtimeAuthenticatedFlag.require(this)
        binding.mainNavigation.setupWithNavController(bottomNavController)
        presenter.attach(this)
        presenter.performFullDataRefresh()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun performFullDataRefresh() =
        presenter.performFullDataRefresh()

    override fun supplyFullDataRefreshStatusFlow(): Flow<DataRefreshStatus.Finished> =
        presenter.dataRefreshFinishedStatusFlow
}
