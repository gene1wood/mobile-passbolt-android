package com.passbolt.mobile.android.feature.resources.update.fieldsgenerator

import com.passbolt.mobile.android.core.commonresource.ResourceTypeFactory
import com.passbolt.mobile.android.database.impl.resourcetypes.GetResourceTypeWithFieldsByIdUseCase
import com.passbolt.mobile.android.feature.resources.update.ResourceValue
import com.passbolt.mobile.android.feature.secrets.usecase.decrypt.parser.SecretParser
import com.passbolt.mobile.android.ui.ResourceModel

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
class EditFieldsModelCreator(
    private val getResourceTypeWithFieldsByIdUseCase: GetResourceTypeWithFieldsByIdUseCase,
    private val secretParser: SecretParser,
    private val resourceTypeEnumFactory: ResourceTypeFactory,
    private val resourceFieldsComparator: ResourceFieldsComparator
) {

    suspend fun create(
        existingResource: ResourceModel,
        secret: ByteArray
    ): List<ResourceValue> {

        val editedResourceType = getResourceTypeWithFieldsByIdUseCase.execute(
            GetResourceTypeWithFieldsByIdUseCase.Input(existingResource.resourceTypeId)
        )

        val resourceTypeEnum = resourceTypeEnumFactory.getResourceTypeEnum(existingResource.resourceTypeId)

        return editedResourceType
            .fields
            .sortedWith(resourceFieldsComparator)
            .map { field ->
                val initialValue = when (field.name) {
                    in listOf(FieldNamesMapper.PASSWORD_FIELD, FieldNamesMapper.SECRET_FIELD) -> {
                        secretParser.extractPassword(resourceTypeEnum, secret)
                    }
                    FieldNamesMapper.DESCRIPTION_FIELD -> {
                        when (resourceTypeEnum) {
                            ResourceTypeFactory.ResourceTypeEnum.SIMPLE_PASSWORD -> {
                                existingResource.description
                            }
                            ResourceTypeFactory.ResourceTypeEnum.PASSWORD_WITH_DESCRIPTION -> {
                                secretParser.extractDescription(resourceTypeEnum, secret)
                            }
                        }
                    }
                    else -> {
                        when (field.name) {
                            FieldNamesMapper.NAME_FIELD -> existingResource.name
                            FieldNamesMapper.USERNAME_FIELD -> existingResource.username
                            FieldNamesMapper.URI_FIELD -> existingResource.url
                            else -> ""
                        }
                    }
                }
                ResourceValue(field, initialValue)
            }
    }
}
