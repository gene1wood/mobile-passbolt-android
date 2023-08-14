package com.passbolt.mobile.android.feature.setup.summary

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.io.Serializable
import com.passbolt.mobile.android.core.localization.R as LocalizationR

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
sealed class ResultStatus(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val buttonText: Int
) : Serializable {
    class Success(
        val userId: String
    ) : ResultStatus(
        com.passbolt.mobile.android.core.ui.R.drawable.ic_success,
        LocalizationR.string.scan_qr_summary_success_title,
        LocalizationR.string.continue_label
    )

    class Failure(
        val message: String
    ) : ResultStatus(
        com.passbolt.mobile.android.core.ui.R.drawable.ic_failed,
        LocalizationR.string.scan_qr_summary_failure_title,
        LocalizationR.string.try_again
    )

    class NoNetwork : ResultStatus(
        com.passbolt.mobile.android.core.ui.R.drawable.ic_failed,
        LocalizationR.string.scan_qr_summary_no_network_title,
        LocalizationR.string.try_again
    )

    class HttpNotSupported : ResultStatus(
        com.passbolt.mobile.android.core.ui.R.drawable.ic_failed,
        LocalizationR.string.scan_qr_summary_http_not_supported_title,
        LocalizationR.string.try_again
    )

    class AlreadyLinked : ResultStatus(
        com.passbolt.mobile.android.core.ui.R.drawable.ic_already_connected,
        LocalizationR.string.scan_qr_summary_already_linked_title,
        LocalizationR.string.continue_label
    )
}
