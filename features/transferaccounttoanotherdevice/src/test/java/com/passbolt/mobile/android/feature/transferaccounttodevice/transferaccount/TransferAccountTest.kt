package com.passbolt.mobile.android.feature.transferaccounttodevice.transferaccount

import com.passbolt.mobile.android.feature.transferaccounttoanotherdevice.transferaccount.TransferAccountContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

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
class TransferAccountTest : KoinTest {

    private val presenter: TransferAccountContract.Presenter by inject()
    private val view: TransferAccountContract.View = mock()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(transferAccountModule)
    }

    @Test
    fun `click cancel transfer button should display cancel transfer dialog`() {
        presenter.attach(view)
        presenter.cancelTransferButtonClick()

        verify(view).showCancelTransferDialog()
    }

    @Test
    fun `click cancel transfer button should display cancel transfer dialog and click stop transfer should close activity`() {
        presenter.attach(view)
        presenter.cancelTransferButtonClick()
        presenter.stopTransferClick()

        verify(view).showCancelTransferDialog()
        verify(view).finish()
    }
}