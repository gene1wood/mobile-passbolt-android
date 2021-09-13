package com.passbolt.mobile.android.feature.authentication.auth.presenter

import com.passbolt.mobile.android.common.FingerprintInformationProvider
import com.passbolt.mobile.android.common.extension.erase
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.feature.setup.enterpassphrase.VerifyPassphraseUseCase
import com.passbolt.mobile.android.storage.cache.passphrase.PassphraseMemoryCache
import com.passbolt.mobile.android.storage.encrypted.biometric.BiometricCipher
import com.passbolt.mobile.android.storage.usecase.accountdata.GetAccountDataUseCase
import com.passbolt.mobile.android.storage.usecase.biometrickey.RemoveBiometricKeyUseCase
import com.passbolt.mobile.android.storage.usecase.passphrase.CheckIfPassphraseFileExistsUseCase
import com.passbolt.mobile.android.storage.usecase.passphrase.GetPassphraseUseCase
import com.passbolt.mobile.android.storage.usecase.passphrase.RemoveSelectedAccountPassphraseUseCase
import com.passbolt.mobile.android.storage.usecase.privatekey.GetPrivateKeyUseCase
import javax.crypto.Cipher

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
@Suppress("LongParameterList") // TODO extract interactors
// presenter for sign in view used for just obtaining the passphrase in the cache without performing API sign in
// handles storing passphrase in cache after sign in button clicked
class PassphrasePresenter(
    private val passphraseMemoryCache: PassphraseMemoryCache,
    getPrivateKeyUseCase: GetPrivateKeyUseCase,
    verifyPassphraseUseCase: VerifyPassphraseUseCase,
    fingerprintInfoProvider: FingerprintInformationProvider,
    removeSelectedAccountPassphraseUseCase: RemoveSelectedAccountPassphraseUseCase,
    checkIfPassphraseFileExistsUseCase: CheckIfPassphraseFileExistsUseCase,
    getAccountDataUseCase: GetAccountDataUseCase,
    coroutineLaunchContext: CoroutineLaunchContext,
    biometricCipher: BiometricCipher,
    getPassphraseUseCase: GetPassphraseUseCase,
    removeBiometricKeyUseCase: RemoveBiometricKeyUseCase
) : AuthBasePresenter(
    getAccountDataUseCase,
    checkIfPassphraseFileExistsUseCase,
    fingerprintInfoProvider,
    removeSelectedAccountPassphraseUseCase,
    getPrivateKeyUseCase,
    verifyPassphraseUseCase,
    biometricCipher,
    getPassphraseUseCase,
    passphraseMemoryCache,
    removeBiometricKeyUseCase,
    coroutineLaunchContext
) {

    override fun onPassphraseVerified(passphrase: ByteArray) {
        passphraseMemoryCache.set(passphrase)
        passphrase.erase()
        view?.apply {
            clearPassphraseInput()
            authSuccess()
        }
    }

    override fun biometricAuthSuccess(authenticatedCipher: Cipher?) {
        super.biometricAuthSuccess(authenticatedCipher)
        view?.authSuccess()
    }

    override fun fingerprintServerConfirmationClick(fingerprint: String) {
        // not used
    }
}
