package com.passbolt.mobile.android.entity.resource

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

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

const val RESOURCE_TYPE_ID = "resourceTypeId"
const val RESOURCE_FIELD_ID = "resourceFieldId"

@Entity
data class ResourceType(
    @PrimaryKey
    val resourceTypeId: String,
    val name: String
)

@Entity
data class ResourceField(
    @PrimaryKey(autoGenerate = true)
    val resourceFieldId: Long = 0,
    val name: String,
    val isSecret: Boolean,
    val maxLength: Int?,
    val isRequired: Boolean,
    val type: String
)

@Entity(primaryKeys = [RESOURCE_TYPE_ID, RESOURCE_FIELD_ID])
data class ResourceTypesAndFieldsCrossRef(
    val resourceTypeId: String,
    val resourceFieldId: Long
)

data class ResourceTypeIdWithFields(
    @Embedded val resourceType: ResourceType,
    @Relation(
        parentColumn = RESOURCE_TYPE_ID,
        entityColumn = RESOURCE_FIELD_ID,
        associateBy = Junction(ResourceTypesAndFieldsCrossRef::class)
    )
    val resourceFields: List<ResourceField>
)
