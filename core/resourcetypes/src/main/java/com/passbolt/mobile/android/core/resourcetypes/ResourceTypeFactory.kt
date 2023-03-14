package com.passbolt.mobile.android.core.resourcetypes

import com.passbolt.mobile.android.database.DatabaseProvider
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
class ResourceTypeFactory(
    private val databaseProvider: DatabaseProvider,
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase
) {

    suspend fun getResourceTypeEnum(resourceTypeId: String): ResourceTypeEnum {
        val selectedAccount = requireNotNull(getSelectedAccountUseCase.execute(Unit).selectedAccount)
        val resourceType = databaseProvider.get(selectedAccount)
            .resourceTypesDao()
            .getResourceTypeWithFieldsById(resourceTypeId)

        return when (val slug = resourceType.resourceType.slug) {
            SLUG_SIMPLE_PASSWORD -> ResourceTypeEnum.SIMPLE_PASSWORD
            SLUG_PASSWORD_WITH_DESCRIPTION -> ResourceTypeEnum.PASSWORD_WITH_DESCRIPTION
            SLUG_TOTP -> ResourceTypeEnum.STANDALONE_TOTP
            else -> throw UnsupportedOperationException("Unknown resource type with slug: $slug")
        }
    }

    enum class ResourceTypeEnum {
        SIMPLE_PASSWORD,
        PASSWORD_WITH_DESCRIPTION,
        STANDALONE_TOTP
    }

    companion object {
        const val SLUG_SIMPLE_PASSWORD = "password-string"
        const val SLUG_PASSWORD_WITH_DESCRIPTION = "password-with-description"
        const val SLUG_TOTP = "totp"
    }
}
