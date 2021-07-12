package com.passbolt.mobile.android.feature.authentication.auth.accountslist

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.passbolt.mobile.android.entity.account.AccountEntity
import com.passbolt.mobile.android.feature.authentication.accountslist.AccountsListContract
import com.passbolt.mobile.android.mappers.AccountModelMapper
import com.passbolt.mobile.android.storage.usecase.accounts.GetAllAccountsDataUseCase
import com.passbolt.mobile.android.ui.AccountModelUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject

/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2021 Passbolt SA
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License (AGPL) as published by the Free Software Foundation version 3.
 *
 * The name "Passbolt" is a registered trademark of Passbolt SA, and Passbolt SA hereby declines to grant a trademark
 * license to "Passbolt" pursuant to the GNU Affero General Public License version 3 Section 7(e), without a separate
 * agreement with Passbolt SA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */
@ExperimentalCoroutinesApi
class AccountListPresenterTest : KoinTest {

    private val presenter: AccountsListContract.Presenter by inject()
    private val view = mock<AccountsListContract.View>()
    private val accountEntityToUiMapper: AccountModelMapper by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(testAccountListModule)
    }

    @Test
    fun `test if account list is displayed with add new account at start`() {
        whenever(mockGetAllAccountsDataUseCase.execute(Unit)).doReturn(GetAllAccountsDataUseCase.Output(SAVED_ACCOUNTS))

        presenter.attach(view)

        argumentCaptor<List<AccountModelUi>>().apply {
            verify(view).showAccounts(capture())
            // verify list content
            assertThat(firstValue.size).isEqualTo(SAVED_ACCOUNTS.size + 1)
            assertThat(firstValue).isEqualTo(accountEntityToUiMapper.map(SAVED_ACCOUNTS))

            // verify if add new account button is added
            assertThat(firstValue).contains(AccountModelUi.AddNewAccount)
        }
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `test if turing on remove mode updates view correct`() {
        whenever(mockGetAllAccountsDataUseCase.execute(Unit)).doReturn(GetAllAccountsDataUseCase.Output(SAVED_ACCOUNTS))

        presenter.attach(view)
        presenter.removeAnAccountClick()

        argumentCaptor<List<AccountModelUi>>().apply {
            verify(view, times(2)).showAccounts(capture())
            // verify if add new account button is hid
            assertThat(secondValue).doesNotContain(AccountModelUi.AddNewAccount)
        }
        verify(view).hideRemoveAccounts()
        verify(view).showDoneRemovingAccounts()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `test if turing off remove mode updates view correct`() {
        whenever(mockGetAllAccountsDataUseCase.execute(Unit)).doReturn(GetAllAccountsDataUseCase.Output(SAVED_ACCOUNTS))

        presenter.attach(view)
        presenter.doneRemovingAccountsClick()

        argumentCaptor<List<AccountModelUi>>().apply {
            verify(view, times(2)).showAccounts(capture())
            // verify if add new account button is shown
            assertThat(secondValue).contains(AccountModelUi.AddNewAccount)
        }
        verify(view).showRemoveAccounts()
        verify(view).hideDoneRemovingAccounts()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `test view shows confirm remove account dialog`() {
        whenever(mockGetAllAccountsDataUseCase.execute(Unit)).doReturn(GetAllAccountsDataUseCase.Output(SAVED_ACCOUNTS))

        presenter.attach(view)
        val accountToRemove = accountEntityToUiMapper.map(SAVED_ACCOUNTS)[0] as AccountModelUi.AccountModel
        presenter.removeAccountClick(accountToRemove)

        verify(view).showAccounts(any())
        verify(view).showRemoveAccountConfirmationDialog(accountToRemove)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `test view shows updated account list after removal`() {
        val mutableAccountList = SAVED_ACCOUNTS.toMutableList()
        whenever(mockGetAllAccountsDataUseCase.execute(Unit)).doReturn(
            GetAllAccountsDataUseCase.Output(mutableAccountList)
        )
        mockRemoveAllAccountsDataUseCase.stub {
            onBlocking { execute(any()) }.then { mutableAccountList.removeAt(0) }
        }

        presenter.attach(view)
        val accountToRemove = accountEntityToUiMapper.map(SAVED_ACCOUNTS)[0]
        presenter.confirmRemoveAccountClick(accountToRemove as AccountModelUi.AccountModel)

        argumentCaptor<List<AccountModelUi>>().apply {
            verify(view, times(3)).showAccounts(capture())
            assertThat(thirdValue.size).isEqualTo(1)
            assertThat(thirdValue[0]).isInstanceOf(AccountModelUi.AddNewAccount::class.java)
        }
        verify(view).hideDoneRemovingAccounts()
        verify(view).showRemoveAccounts()
        verifyNoMoreInteractions(view)
    }

    private companion object {
        private val SAVED_ACCOUNTS = listOf(
            AccountEntity(userId = "1", null, null, null, null, "dev.test", "server_id")
        )

    }

}
