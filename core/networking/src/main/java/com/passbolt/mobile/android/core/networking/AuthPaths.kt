package com.passbolt.mobile.android.core.networking

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
object AuthPaths {
    const val AUTH_VERIFY = "/auth/verify.json"
    const val AUTH_RSA = "/auth/jwt/rsa.json"
    const val AUTH_SIGN_IN = "/auth/jwt/login.json"
    const val AUTH_SIGN_OUT = "/auth/jwt/logout.json"
    const val AUTH_JWT_REFRESH = "/auth/jwt/refresh.json"
    const val MFA_VERIFICATION_TOTP = "/mfa/verify/totp.json"
    const val MFA_VERIFICATION_YUBIKEY = "/mfa/verify/yubikey.json"
    const val MFA_VERIFICATION_DUO_PROMPT = "/mfa/verify/duo/prompt"
    const val MFA_VERIFICATION_DUO_VERIFY = "/mfa/verify/duo/callback"
    const val AVATAR_PATH = "/img/avatar/"
    const val TRANSFER_PATH = "/mobile/transfers/"
}
