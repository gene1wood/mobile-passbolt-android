package com.passbolt.mobile.android.feature.resources.tags

import com.passbolt.mobile.android.database.impl.resources.GetLocalResourceTagsUseCase
import com.passbolt.mobile.android.database.impl.resources.GetLocalResourceUseCase
import com.passbolt.mobile.android.permissions.permissions.PermissionsMode
import com.passbolt.mobile.android.ui.ResourceModel
import com.passbolt.mobile.android.ui.ResourcePermission
import com.passbolt.mobile.android.ui.TagModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.time.ZonedDateTime

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

class ResourceTagsPresenterTest : KoinTest {

    private val presenter: ResourceTagsContract.Presenter by inject()
    private val view: ResourceTagsContract.View = mock()

    @ExperimentalCoroutinesApi
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(testResourceTagsModule)
    }

    @Before
    fun setup() {
        mockGetLocalResourceUseCase.stub {
            onBlocking { execute(GetLocalResourceUseCase.Input(RESOURCE_MODEL.resourceId)) }
                .doReturn(GetLocalResourceUseCase.Output(RESOURCE_MODEL))
        }
        mockResourceTagsUseCase.stub {
            onBlocking { execute(any()) }.doReturn(GetLocalResourceTagsUseCase.Output(RESOURCE_TAGS))
        }
        presenter.attach(view)
    }

    @Test
    fun `resource header and tags list should be shown correct`() {
        presenter.argsRetrieved(
            RESOURCE_MODEL.resourceId,
            PermissionsMode.VIEW
        )

        verify(view).showFavouriteStar()
        verify(view).displayTitle(NAME)
        verify(view).displayInitialsIcon(NAME, INITIALS)
        verify(view).showTags(RESOURCE_TAGS)
        verifyNoMoreInteractions(view)
    }

    private companion object {
        private const val NAME = "name"
        private const val USERNAME = "username"
        private const val INITIALS = "NN"
        private const val URL = "https://www.passbolt.com"
        private const val ID = "id"
        private const val DESCRIPTION = "desc"
        private const val RESOURCE_TYPE_ID = "resTypeId"
        private const val FOLDER_ID_ID = "folderId"
        private val RESOURCE_MODEL = ResourceModel(
            ID,
            RESOURCE_TYPE_ID,
            FOLDER_ID_ID,
            NAME,
            USERNAME,
            null,
            INITIALS,
            URL,
            DESCRIPTION,
            ResourcePermission.READ,
            "fav-id",
            ZonedDateTime.now()
        )
        private val RESOURCE_TAGS = listOf(
            TagModel("id1", "tag1", false),
            TagModel("id2", "tag2", false)
        )
    }
}
