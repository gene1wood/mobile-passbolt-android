package com.passbolt.mobile.android.serializers.gson.validation

import androidx.annotation.VisibleForTesting
import com.passbolt.mobile.android.common.validation.OptionalStringLengthValidation
import com.passbolt.mobile.android.common.validation.RequiredStringLengthValidation
import com.passbolt.mobile.android.dto.response.ResourceResponseDto

class TotpResourceValidation : ResourceValidation {

    override fun invoke(resource: ResourceResponseDto): Boolean {
        val validationResults = listOf(
            RequiredStringLengthValidation().invoke(resource.name, TOTP_NAME_MIN_LENGTH, TOTP_NAME_MAX_LENGTH),
            OptionalStringLengthValidation().invoke(resource.uri, TOTP_URI_MIN_LENGTH, TOTP_URI_MAX_LENGTH)
        )

        return validationResults.all { it }
    }

    @VisibleForTesting
    companion object {
        const val TOTP_NAME_MIN_LENGTH = 0
        const val TOTP_NAME_MAX_LENGTH = 255
        const val TOTP_URI_MIN_LENGTH = 0
        const val TOTP_URI_MAX_LENGTH = 1024
    }
}
