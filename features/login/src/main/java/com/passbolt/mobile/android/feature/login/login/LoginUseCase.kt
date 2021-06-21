package com.passbolt.mobile.android.feature.login.login

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.networking.NetworkResult
import com.passbolt.mobile.android.mappers.LoginMapper
import com.passbolt.mobile.android.service.auth.AuthRepository

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
class LoginUseCase(
    private val authRepository: AuthRepository,
    private val loginMapper: LoginMapper
) : AsyncUseCase<LoginUseCase.Input, LoginUseCase.Output> {

    override suspend fun execute(input: Input): Output =
        when (val result = authRepository.login(loginMapper.mapRequestToDto(input.userId, input.challenge))) {
            is NetworkResult.Failure.NetworkError -> Output.Failure
            is NetworkResult.Failure.ServerError -> Output.Failure
            is NetworkResult.Success -> Output.Success(result.value.body.challenge)
        }

    sealed class Output {
        class Success(
            val challenge: String
        ) : Output()

        object Failure : Output()
    }

    data class Input(
        val userId: String,
        val challenge: String
    )
}
