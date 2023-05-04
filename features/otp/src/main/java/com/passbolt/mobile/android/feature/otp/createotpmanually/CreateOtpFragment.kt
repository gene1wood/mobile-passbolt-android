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

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.passbolt.mobile.android.common.extension.setDebouncingOnClick
import com.passbolt.mobile.android.core.extension.initDefaultToolbar
import com.passbolt.mobile.android.core.extension.showSnackbar
import com.passbolt.mobile.android.core.ui.progressdialog.hideProgressDialog
import com.passbolt.mobile.android.core.ui.progressdialog.showProgressDialog
import com.passbolt.mobile.android.core.ui.textinputfield.StatefulInput.State.Error
import com.passbolt.mobile.android.feature.authentication.BindingScopedAuthenticatedFragment
import com.passbolt.mobile.android.feature.otp.R
import com.passbolt.mobile.android.feature.otp.createotpmanuallyexpertsettings.CreateOtpAdvancedSettingsFragment
import com.passbolt.mobile.android.feature.otp.databinding.FragmentCreateOtpBinding
import com.passbolt.mobile.android.ui.OtpAdvancedSettingsModel
import org.koin.android.ext.android.inject

class CreateOtpFragment :
    BindingScopedAuthenticatedFragment<FragmentCreateOtpBinding, CreateOtpContract.View>(
        FragmentCreateOtpBinding::inflate
    ),
    CreateOtpContract.View {

    override val presenter: CreateOtpContract.Presenter by inject()
    private val otpSettingsModifiedListener = { _: String, bundle: Bundle ->
        val algorithm = requireNotNull(bundle.getString(CreateOtpAdvancedSettingsFragment.EXTRA_ALGORITHM))
        val period = requireNotNull(bundle.getInt(CreateOtpAdvancedSettingsFragment.EXTRA_PERIOD))
        val digits = requireNotNull(bundle.getInt(CreateOtpAdvancedSettingsFragment.EXTRA_DIGITS))
        presenter.otpSettingsModified(algorithm, period, digits)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableSaveInstanceStateForInputs()
        setListeners()
        initDefaultToolbar(binding.toolbar)
        presenter.attach(this)
    }

    private fun disableSaveInstanceStateForInputs() {
        setOf(binding.totpLabelInput, binding.totpIssuerInput, binding.totpSecretInput)
            .forEach { it.disableSavingInstanceState() }
    }

    private fun setListeners() {
        with(binding) {
            totpLabelInput.setTextChangeListener {
                presenter.totpLabelChanged(it)
            }
            totpIssuerInput.setTextChangeListener {
                presenter.totpIssuerChanged(it)
            }
            totpSecretInput.setTextChangeListener {
                presenter.totpSecretChanged(it)
            }
            advancedSettings.setDebouncingOnClick {
                presenter.advancedSettingsClick()
            }
            createButton.setDebouncingOnClick {
                presenter.createClick()
            }
        }
    }

    override fun navigateToCreateOtpAdvancedSettings(advancedSettingsModel: OtpAdvancedSettingsModel) {
        setFragmentResultListener(
            CreateOtpAdvancedSettingsFragment.REQUEST_MODIFY_TOTP_SETTINGS,
            otpSettingsModifiedListener
        )
        findNavController().navigate(
            CreateOtpFragmentDirections.actionCreateOtpManuallyFragmentToCreateOtpAdvancedSettingsFragment(
                advancedSettingsModel
            )
        )
    }

    override fun showLabelValidationError(maxLength: Int) {
        binding.totpLabelInput.setState(
            Error(getString(R.string.validation_required_with_max_length, maxLength))
        )
    }

    override fun showSecretValidationError(maxLength: Int) {
        binding.totpSecretInput.setState(
            Error(getString(R.string.validation_required_with_max_length, maxLength))
        )
    }

    override fun showIssuerValidationError(maxLength: Int) {
        binding.totpIssuerInput.setState(
            Error(getString(R.string.validation_required_with_max_length, maxLength))
        )
    }

    override fun showProgress() {
        showProgressDialog(childFragmentManager)
    }

    override fun hideProgress() {
        hideProgressDialog(childFragmentManager)
    }

    override fun showGenericError() {
        showSnackbar(
            R.string.common_failure,
            backgroundColor = R.color.red
        )
    }

    override fun showEncryptionError(message: String) {
        showSnackbar(
            R.string.resource_permissions_secret_encrypt_failure,
            backgroundColor = R.color.red
        )
    }

    override fun navigateToOtpList(otpCreated: Boolean) {
        setFragmentResult(
            REQUEST_CREATE_OTP,
            bundleOf(EXTRA_OTP_CREATED to otpCreated)
        )
        findNavController().popBackStack()
    }

    override fun setValues(label: String, issuer: String, secret: String) {
        with(binding) {
            totpLabelInput.text = label
            totpIssuerInput.text = issuer
            totpSecretInput.text = secret
        }
    }

    companion object {
        const val REQUEST_CREATE_OTP = "CREATE_OTP"
        const val EXTRA_OTP_CREATED = "OTP_CREATED"
    }
}
