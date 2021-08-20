package com.password.mobile.android.feature.home

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.passbolt.mobile.android.core.commonresource.GetResourcesUseCase
import com.passbolt.mobile.android.core.mvp.session.UnauthenticatedReason
import com.passbolt.mobile.android.core.networking.NetworkResult
import com.passbolt.mobile.android.dto.response.ResourceResponseDto
import com.passbolt.mobile.android.feature.home.screen.HomeContract
import com.passbolt.mobile.android.feature.home.screen.HomePresenter
import com.passbolt.mobile.android.feature.secrets.usecase.decrypt.SecretInteractor
import com.passbolt.mobile.android.storage.usecase.accountdata.GetSelectedAccountDataUseCase
import com.passbolt.mobile.android.storage.usecase.selectedaccount.GetSelectedAccountUseCase
import com.passbolt.mobile.android.ui.ResourceModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.io.IOException

@ExperimentalCoroutinesApi
class HomePresenterTest : KoinTest {

    private val presenter: HomeContract.Presenter by inject()
    private val view: HomeContract.View = mock()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(testHomeModule)
    }

    @Before
    fun setUp() {
        whenever(getSelectedAccountUseCase.execute(anyOrNull())).thenReturn(
            GetSelectedAccountUseCase.Output("id")
        )
    }

    @Test
    fun `empty user avatar should not be displayed`() = runBlockingTest {
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Success(emptyList())
        )
        mockAccountData(null)
        presenter.attach(view)
        verify(view, never()).displayAvatar(anyOrNull())
    }

    @Test
    fun `user avatar should be displayed when provided`() = runBlockingTest {
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Success(emptyList())
        )
        val url = "avatar_url"
        mockAccountData(url)
        presenter.attach(view)
        verify(view).displayAvatar(eq(url))
    }

    @Test
    fun `all fetched resources should be displayed when empty search text`() = runBlockingTest {
        val mockedList = mockResourcesList()
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Success(mockedList)
        )
        mockAccountData(null)
        presenter.attach(view)
        verify(view).hideRefreshProgress()
        verify(view).hideProgress()
        verify(view).showPasswords(anyOrNull())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `refresh swiped should reload data with filter applied when search text entered`() = runBlockingTest {
        val mockedList = mockResourcesList()
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Success(mockedList)
        )
        mockAccountData(null)
        presenter.attach(view)
        presenter.searchTextChange("second")
        reset(view)
        presenter.refreshSwipe()
        verify(view).hideRefreshProgress()
        verify(view).hideProgress()
        verify(view).showPasswords(anyOrNull())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `empty view should be displayed when search is empty`() = runBlockingTest {
        val mockedList = mockResourcesList()
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Success(mockedList)
        )
        mockAccountData(null)
        presenter.attach(view)
        presenter.searchTextChange("third")
        reset(view)
        presenter.refreshSwipe()
        verify(view).hideRefreshProgress()
        verify(view).hideProgress()
        verify(view).showSearchEmptyList()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `empty view should be displayed when there are no resources`() = runBlockingTest {
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Success(emptyList())
        )
        mockAccountData(null)
        presenter.attach(view)
        verify(view).hideRefreshProgress()
        verify(view).hideProgress()
        verify(view).showEmptyList()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `error should be displayed when request failures`() = runBlockingTest {
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Failure(NetworkResult.Failure.ServerError(IOException(), headerMessage = ""))
        )
        mockAccountData(null)
        presenter.attach(view)
        verify(view).hideRefreshProgress()
        verify(view).hideProgress()
        verify(view).showError()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `refresh clicked should fetch resources`() = runBlockingTest {
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Failure(NetworkResult.Failure.ServerError(IOException(), headerMessage = ""))
        )
        mockAccountData(null)
        presenter.attach(view)
        reset(view)
        presenter.refreshClick()
        verify(view).showProgress()
        verify(view).hideRefreshProgress()
        verify(view).hideProgress()
        verify(view).showError()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `item clicked should open details screen`() = runBlockingTest {
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Failure(NetworkResult.Failure.ServerError(IOException(), headerMessage = ""))
        )
        val model = ResourceModel("id", "title", "subtitle", "", "initials", "")
        mockAccountData(null)
        presenter.attach(view)
        reset(view)
        presenter.itemClick(model)
        verify(view).navigateToDetails(model)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `3 dots clicked should open more screen`() = runBlockingTest {
        val model = ResourceModel(
            resourceId = "id",
            name = "title",
            username = "subtitle",
            icon = null,
            initials = "T",
            url = ""
        )
        whenever(getResourcesUseCase.execute(anyOrNull())).thenReturn(
            GetResourcesUseCase.Output.Failure(NetworkResult.Failure.ServerError(IOException(), headerMessage = ""))
        )
        mockAccountData(null)
        presenter.attach(view)
        reset(view)
        presenter.moreClick(model)
        verify(view).navigateToMore(model)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `view should show decrypt error correct`() {
        mockSecretInteractor.stub {
            onBlocking { fetchAndDecrypt(ID) }.doReturn(
                SecretInteractor.Output.DecryptFailure(RuntimeException())
            )
        }

        presenter.attach(view)
        presenter.moreClick(RESOURCE_MODEL)
        presenter.menuCopyPasswordClick()

        verify(view).showDecryptionFailure()
    }

    @Test
    fun `view should show fetch error correct`() {
        mockSecretInteractor.stub {
            onBlocking { fetchAndDecrypt(ID) }.doReturn(
                SecretInteractor.Output.FetchFailure(RuntimeException())
            )
        }

        presenter.attach(view)
        presenter.moreClick(RESOURCE_MODEL)
        presenter.menuCopyPasswordClick()

        verify(view).showFetchFailure()
    }

    @Test
    fun `view should show auth when passphrase not in cache`() {
        mockSecretInteractor.stub {
            onBlocking { fetchAndDecrypt(ID) }.doReturn(
                SecretInteractor.Output.Unauthorized
            )
        }

        presenter.attach(view)
        presenter.moreClick(RESOURCE_MODEL)
        presenter.menuCopyPasswordClick()

        verify(view).showAuth(UnauthenticatedReason.PASSPHRASE)
    }

    @Test
    fun `view should copy secret after successful decrypt`() {
        mockSecretInteractor.stub {
            onBlocking { fetchAndDecrypt(ID) }.doReturn(
                SecretInteractor.Output.Success(DECRYPTED_SECRET)
            )
        }

        presenter.attach(view)
        presenter.moreClick(RESOURCE_MODEL)
        presenter.menuCopyPasswordClick()

        verify(view).addToClipboard(HomePresenter.SECRET_LABEL, String(DECRYPTED_SECRET))
    }

    private fun mockResourcesList() = listOf(
        ResourceResponseDto(
            id = "id1",
            description = "first description",
            name = "first name",
            uri = "",
            username = ""
        ), ResourceResponseDto(
            id = "id2",
            description = "second description",
            name = "second name",
            uri = "",
            username = ""
        )
    )


    private fun mockAccountData(avatarUrl: String?) {
        whenever(getSelectedAccountDataUseCase.execute(anyOrNull())).thenReturn(
            GetSelectedAccountDataUseCase.Output(
                firstName = "",
                lastName = "",
                email = "",
                avatarUrl = avatarUrl,
                url = "",
                serverId = ""
            )
        )
    }

    private companion object {
        private const val NAME = "name"
        private const val USERNAME = "username"
        private const val INITIALS = "NN"
        private const val URL = "https://www.passbolt.com"
        private const val SEARCH_CRITERIA = "name username"
        private const val ID = "id"
        private val RESOURCE_MODEL = ResourceModel(
            ID,
            NAME,
            USERNAME,
            null,
            INITIALS,
            URL
        )
        private val DECRYPTED_SECRET = "secret".toByteArray()
    }
}
