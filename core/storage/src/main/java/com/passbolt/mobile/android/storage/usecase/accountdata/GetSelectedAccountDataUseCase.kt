package com.passbolt.mobile.android.storage.usecase.accountdata

import com.passbolt.mobile.android.common.usecase.UseCase
import com.passbolt.mobile.android.storage.encrypted.EncryptedSharedPreferencesFactory
import com.passbolt.mobile.android.storage.paths.AccountDataFileName
import com.passbolt.mobile.android.storage.usecase.AVATAR_URL_KEY
import com.passbolt.mobile.android.storage.usecase.EMAIL_KEY
import com.passbolt.mobile.android.storage.usecase.SERVER_ID_KEY
import com.passbolt.mobile.android.storage.usecase.URL_KEY
import com.passbolt.mobile.android.storage.usecase.USER_FIRST_NAME_KEY
import com.passbolt.mobile.android.storage.usecase.USER_LABEL_KEY
import com.passbolt.mobile.android.storage.usecase.USER_LAST_NAME_KEY
import com.passbolt.mobile.android.storage.usecase.selectedaccount.GetSelectedAccountUseCase

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

class GetSelectedAccountDataUseCase(
    private val encryptedSharedPreferencesFactory: EncryptedSharedPreferencesFactory,
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase
) : UseCase<Unit, GetSelectedAccountDataUseCase.Output> {

    override fun execute(input: Unit): Output {
        val userId = getSelectedAccountUseCase.execute(Unit).selectedAccount
        val fileName = AccountDataFileName(requireNotNull(userId)).name
        val sharedPreferences = encryptedSharedPreferencesFactory.get("$fileName.xml")

        return Output(
            firstName = sharedPreferences.getString(USER_FIRST_NAME_KEY, null),
            lastName = sharedPreferences.getString(USER_LAST_NAME_KEY, null),
            email = sharedPreferences.getString(EMAIL_KEY, null),
            avatarUrl = sharedPreferences.getString(AVATAR_URL_KEY, null),
            url = sharedPreferences.getString(URL_KEY, "").orEmpty(),
            serverId = sharedPreferences.getString(SERVER_ID_KEY, ""),
            label = sharedPreferences.getString(USER_LABEL_KEY, null)
        )
    }

    class Output(
        val firstName: String?,
        val lastName: String?,
        val email: String?,
        val avatarUrl: String?,
        val url: String,
        val serverId: String?,
        val label: String?
    )
}
