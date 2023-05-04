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

package com.passbolt.mobile.android.feature.otp.createotpmanually

import com.passbolt.mobile.android.common.validation.StringMaxLength
import com.passbolt.mobile.android.common.validation.StringNotBlank
import com.passbolt.mobile.android.common.validation.validation
import com.passbolt.mobile.android.core.mvp.authentication.BaseAuthenticatedPresenter
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.core.resources.usecase.CreateTotpResourceUseCase
import com.passbolt.mobile.android.core.resourcetypes.ResourceTypeFactory
import com.passbolt.mobile.android.database.impl.resourcetypes.GetResourceTypeWithFieldsBySlugUseCase
import com.passbolt.mobile.android.feature.authentication.session.runAuthenticatedOperation
import com.passbolt.mobile.android.feature.otp.scanotp.parser.OtpParseResult
import com.passbolt.mobile.android.ui.OtpAdvancedSettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CreateOtpPresenter(
    private val createTotpResourceUseCase: CreateTotpResourceUseCase,
    private val getResourceTypeWithFieldsBySlugUseCase: GetResourceTypeWithFieldsBySlugUseCase,
    coroutineLaunchContext: CoroutineLaunchContext
) : BaseAuthenticatedPresenter<CreateOtpContract.View>(coroutineLaunchContext), CreateOtpContract.Presenter {

    override var view: CreateOtpContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + coroutineLaunchContext.ui)

    private var algorithm = OtpParseResult.OtpQr.Algorithm.DEFAULT.name
    private var period = OtpParseResult.OtpQr.TotpQr.DEFAULT_PERIOD_SECONDS
    private var digits = OtpParseResult.OtpQr.TotpQr.DEFAULT_DIGITS
    private var secret = ""
    private var label = ""
    private var issuer = ""

    override fun attach(view: CreateOtpContract.View) {
        super<BaseAuthenticatedPresenter>.attach(view)
        view.setValues(label, issuer, secret)
    }

    override fun otpSettingsModified(algorithm: String, period: Int, digits: Int) {
        this.algorithm = algorithm
        this.period = period
        this.digits = digits
    }

    override fun totpLabelChanged(label: String) {
        this.label = label
    }

    override fun totpSecretChanged(secret: String) {
        this.secret = secret
    }

    override fun totpIssuerChanged(issuer: String) {
        this.issuer = issuer
    }

    override fun createClick() {
        validation {
            of(label) {
                withRules(StringNotBlank, StringMaxLength(LABEL_MAX_LENGTH)) {
                    onInvalid { view?.showLabelValidationError(LABEL_MAX_LENGTH) }
                }
            }
            of(issuer) {
                withRules(StringMaxLength(ISSUER_MAX_LENGTH)) {
                    onInvalid { view?.showIssuerValidationError(ISSUER_MAX_LENGTH) }
                }
            }
            of(secret) {
                withRules(StringNotBlank, StringMaxLength(SECRET_MAX_LENGTH)) {
                    onInvalid { view?.showSecretValidationError(SECRET_MAX_LENGTH) }
                }
            }
            onValid { createTotp() }
        }
    }

    private fun createTotp() {
        scope.launch {
            view?.showProgress()
            when (val result = runAuthenticatedOperation(needSessionRefreshFlow, sessionRefreshedFlow) {
                createTotpResourceUseCase.execute(createResourceInput())
            }) {
                is CreateTotpResourceUseCase.Output.Failure<*> -> view?.showGenericError()
                is CreateTotpResourceUseCase.Output.OpenPgpError -> view?.showEncryptionError(result.message)
                is CreateTotpResourceUseCase.Output.PasswordExpired -> {
                    /* will not happen in BaseAuthenticatedPresenter */
                }
                is CreateTotpResourceUseCase.Output.Success -> {
                    view?.navigateToOtpList(otpCreated = true)
                }
            }
            view?.hideProgress()
        }
    }

    private suspend fun createResourceInput(): CreateTotpResourceUseCase.Input {
        val totpResourceType = getResourceTypeWithFieldsBySlugUseCase.execute(
            GetResourceTypeWithFieldsBySlugUseCase.Input(ResourceTypeFactory.SLUG_TOTP)
        )
        return CreateTotpResourceUseCase.Input(
            resourceTypeId = totpResourceType.resourceTypeId,
            issuer = issuer,
            label = label,
            period = period,
            digits = digits,
            algorithm = algorithm,
            secretKey = secret
        )
    }

    override fun advancedSettingsClick() {
        view?.navigateToCreateOtpAdvancedSettings(
            OtpAdvancedSettingsModel(
                period = period,
                algorithm = algorithm,
                digits = digits
            )
        )
    }

    private companion object {
        private const val LABEL_MAX_LENGTH = 255
        private const val ISSUER_MAX_LENGTH = 255
        private const val SECRET_MAX_LENGTH = 1024
    }
}
