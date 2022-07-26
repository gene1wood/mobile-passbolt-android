package com.password.mobile.android.feature.home.switchaccount

import com.passbolt.mobile.android.core.navigation.AppContext
import com.passbolt.mobile.android.feature.home.switchaccount.SwitchAccountContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class SwitchAccountTest : KoinTest {

    private val presenter: SwitchAccountContract.Presenter by inject()
    private val view: SwitchAccountContract.View = mock()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(testSwitchAccountModule)
    }

    @Test
    fun `sign out click should sign out the user`() = runTest {
        presenter.attach(view)
        presenter.signOutClick()
        presenter.signOutConfirmed()

        verify(view).showSignOutDialog()
        verify(mockSignOutUseCase).execute(Unit)
        verify(view).showProgress()
        verify(view).hideProgress()
        verify(view).navigateToStartup()
    }

    @Test
    fun `account list should be shown on ui`() {
        val appContext = AppContext.APP
        val uiMapped = switchAccountModelMapper.map(accountsList, appContext)

        presenter.attach(view)
        presenter.argsRetrieved(appContext)

        verify(view).showAccountsList(uiMapped)
    }

    @Test
    fun `see details should show account details`() {
        presenter.attach(view)
        presenter.seeDetailsClick()

        verify(view).navigateToAccountDetails()
    }
}
