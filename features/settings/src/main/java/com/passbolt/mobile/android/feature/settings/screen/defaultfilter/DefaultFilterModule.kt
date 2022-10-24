package com.passbolt.mobile.android.feature.settings.screen.defaultfilter

import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.passbolt.mobile.android.feature.settings.screen.defaultfilter.recycler.DefaultFilterItem
import org.koin.core.module.Module

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

fun Module.defaultFilterModule() {
    scope<DefaultFilterFragment> {
        scoped<DefaultFilterContract.Presenter> {
            DefaultFilterPresenter(
                updateHomeDisplayViewPrefsUseCase = get(),
                homeDisplayViewPrefsValidator = get(),
                getHomeDisplayViewPrefsUseCase = get()
            )
        }
        scoped<ItemAdapter<DefaultFilterItem>> {
            ItemAdapter.items()
        }
        scoped {
            FastAdapter.with(
                get<ItemAdapter<DefaultFilterItem>>()
            )
        }
    }
}
