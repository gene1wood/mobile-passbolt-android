package com.passbolt.mobile.android.storage.usecase.preferences

import com.passbolt.mobile.android.entity.featureflags.FeatureFlagsModel
import com.passbolt.mobile.android.entity.home.HomeDisplayView
import com.passbolt.mobile.android.storage.usecase.featureflags.GetFeatureFlagsUseCase
import com.passbolt.mobile.android.ui.DefaultFilterModel
import kotlinx.coroutines.runBlocking

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
class HomeDisplayViewPrefsValidator(
    private val getFeatureFlagsUseCase: GetFeatureFlagsUseCase
) {

    fun validated(output: GetHomeDisplayViewPrefsUseCase.Output): GetHomeDisplayViewPrefsUseCase.Output {
        val featureFlags = runBlocking { getFeatureFlagsUseCase.execute(Unit).featureFlags }
        val validatedLastUsedView = output.lastUsedHomeView.mutateIf(
            { isNotAvailable(output.lastUsedHomeView, featureFlags) },
            HomeDisplayView.ALL_ITEMS
        )
        val validatedUserSetView = output.userSetHomeView.mutateIf(
            { isNotAvailable(output.userSetHomeView, featureFlags) },
            DefaultFilterModel.ALL_ITEMS
        )
        return output.copy(
            lastUsedHomeView = validatedLastUsedView,
            userSetHomeView = validatedUserSetView
        )
    }

    fun validatedDefaultFiltersList(): List<DefaultFilterModel> {
        val featureFlags = runBlocking { getFeatureFlagsUseCase.execute(Unit).featureFlags }
        return DefaultFilterModel.values().toMutableList()
            .apply {
                if (!featureFlags.areFoldersAvailable) {
                    remove(DefaultFilterModel.FOLDERS)
                }
                if (!featureFlags.areTagsAvailable) {
                    remove(DefaultFilterModel.TAGS)
                }
            }
    }

    private fun <T> T.mutateIf(condition: () -> Boolean, replacement: T) =
        if (condition()) replacement else this

    private fun isNotAvailable(view: DefaultFilterModel, featureFlags: FeatureFlagsModel) =
        (view == DefaultFilterModel.FOLDERS && !featureFlags.areFoldersAvailable) ||
                (view == DefaultFilterModel.TAGS && !featureFlags.areTagsAvailable)

    private fun isNotAvailable(view: HomeDisplayView, featureFlags: FeatureFlagsModel) =
        (view == HomeDisplayView.FOLDERS && !featureFlags.areFoldersAvailable) ||
                (view == HomeDisplayView.TAGS && !featureFlags.areTagsAvailable)
}
