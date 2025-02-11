package com.passbolt.mobile.android.feature.resources.details

import com.passbolt.mobile.android.core.commonfolders.usecase.db.GetLocalFolderLocationUseCase
import com.passbolt.mobile.android.core.fulldatarefresh.DataRefreshStatus
import com.passbolt.mobile.android.core.fulldatarefresh.FullDataRefreshExecutor
import com.passbolt.mobile.android.core.fulldatarefresh.HomeDataInteractor
import com.passbolt.mobile.android.core.resources.actions.SecretPropertiesActionsInteractor
import com.passbolt.mobile.android.core.resources.actions.SecretPropertyActionResult
import com.passbolt.mobile.android.core.resources.usecase.FavouritesInteractor
import com.passbolt.mobile.android.core.resources.usecase.db.GetLocalResourcePermissionsUseCase
import com.passbolt.mobile.android.core.resources.usecase.db.GetLocalResourceTagsUseCase
import com.passbolt.mobile.android.core.resources.usecase.db.GetLocalResourceUseCase
import com.passbolt.mobile.android.core.resourcetypes.ResourceTypeFactory.ResourceTypeEnum.PASSWORD_WITH_DESCRIPTION
import com.passbolt.mobile.android.core.resourcetypes.usecase.db.GetResourceTypeIdToSlugMappingUseCase
import com.passbolt.mobile.android.core.resourcetypes.usecase.db.GetResourceTypeWithFieldsByIdUseCase
import com.passbolt.mobile.android.entity.featureflags.FeatureFlagsModel
import com.passbolt.mobile.android.entity.resource.ResourceField
import com.passbolt.mobile.android.feature.resourcedetails.details.ResourceDetailsContract
import com.passbolt.mobile.android.storage.usecase.featureflags.GetFeatureFlagsUseCase
import com.passbolt.mobile.android.supportedresourceTypes.SupportedContentTypes.PASSWORD_AND_DESCRIPTION_SLUG
import com.passbolt.mobile.android.supportedresourceTypes.SupportedContentTypes.PASSWORD_DESCRIPTION_TOTP_SLUG
import com.passbolt.mobile.android.ui.GroupModel
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.ResourceModel
import com.passbolt.mobile.android.ui.ResourcePermission
import com.passbolt.mobile.android.ui.TagModel
import com.passbolt.mobile.android.ui.UserWithAvatar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime
import java.util.UUID

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

class ResourceDetailsPresenterTest : KoinTest {

    private val presenter: ResourceDetailsContract.Presenter by inject()
    private val view: ResourceDetailsContract.View = mock()
    private val mockFullDataRefreshExecutor: FullDataRefreshExecutor by inject()

    @ExperimentalCoroutinesApi
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(testResourceDetailsModule)
    }

    @Before
    fun setup() {
        mockGetLocalResourceUseCase.stub {
            onBlocking { execute(GetLocalResourceUseCase.Input(RESOURCE_MODEL.resourceId)) }
                .doReturn(GetLocalResourceUseCase.Output(RESOURCE_MODEL))
        }
        mockGetLocalResourcePermissionsUseCase.stub {
            onBlocking { execute(GetLocalResourcePermissionsUseCase.Input(RESOURCE_MODEL.resourceId)) }
                .doReturn(GetLocalResourcePermissionsUseCase.Output(listOf(groupPermission, userPermission)))
        }
        mockGetFeatureFlagsUseCase.stub {
            onBlocking { execute(Unit) } doReturn GetFeatureFlagsUseCase.Output(
                FeatureFlagsModel(
                    privacyPolicyUrl = null,
                    termsAndConditionsUrl = null,
                    isPreviewPasswordAvailable = true,
                    areFoldersAvailable = true,
                    areTagsAvailable = true,
                    isTotpAvailable = true
                )
            )
        }
        mockFavouritesInteractor.stub {
            onBlocking { addToFavouritesAndUpdateLocal(any()) }.doReturn(FavouritesInteractor.Output.Success)
            onBlocking { removeFromFavouritesAndUpdateLocal(any()) }.doReturn(FavouritesInteractor.Output.Success)
        }
        mockResourceTagsUseCase.stub {
            onBlocking { execute(any()) }.doReturn(GetLocalResourceTagsUseCase.Output(RESOURCE_TAGS))
        }
        whenever(mockFullDataRefreshExecutor.dataRefreshStatusFlow).doReturn(
            flowOf(DataRefreshStatus.Finished(HomeDataInteractor.Output.Success))
        )
        mockGetFolderLocationUseCase.stub {
            onBlocking { execute(any()) }.doReturn(GetLocalFolderLocationUseCase.Output(emptyList()))
        }
        mockGetResourceTypeWithFields.stub {
            onBlocking { execute(any()) }.doReturn(
                GetResourceTypeWithFieldsByIdUseCase.Output(
                    resourceTypeId = RESOURCE_MODEL.resourceTypeId,
                    listOf(ResourceField(0, "description", false, null, true, "string"))
                )
            )
        }
        mockGetResourceTypeIdToSlugMappingUseCase.stub {
            onBlocking { execute(Unit) }.doReturn(
                GetResourceTypeIdToSlugMappingUseCase.Output(
                    mapOf(UUID.fromString(RESOURCE_TYPE_ID) to PASSWORD_AND_DESCRIPTION_SLUG)
                )
            )
        }
        presenter.attach(view)
    }

    @Test
    fun `password details should be shown correct`() {
        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)

        verify(view, times(2)).showFavouriteStar()
        verify(view, times(2)).displayTitle(NAME)
        verify(view, times(2)).displayUsername(USERNAME)
        verify(view, times(2)).displayInitialsIcon(NAME, INITIALS)
        verify(view, times(2)).displayUrl(URL)
        verify(view, times(2)).showPasswordHidden()
        verify(view, times(2)).showPasswordHiddenIcon()
        verify(view, times(2)).showPermissions(eq(listOf(groupPermission)), eq(listOf(userPermission)), any(), any())
        verify(view, times(2)).showTags(RESOURCE_TAGS.map { it.slug })
        verify(view, times(2)).showDescription(RESOURCE_MODEL.description!!, useSecretFont = false)
        verify(view, times(2)).showFolderLocation(emptyList())
        verify(view, times(2)).hideTotpSection()
        verify(view).hideRefreshProgress()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `top should show on appropriate content type`() {
        mockGetResourceTypeIdToSlugMappingUseCase.stub {
            onBlocking { execute(Unit) }.doReturn(
                GetResourceTypeIdToSlugMappingUseCase.Output(
                    mapOf(UUID.fromString(RESOURCE_TYPE_ID) to PASSWORD_DESCRIPTION_TOTP_SLUG)
                )
            )
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)

        verify(view, times(2)).showTotpSection()
    }

    @Test
    fun `password details description encryption state should be shown correct for simple password`() {
        mockGetResourceTypeWithFields.stub {
            onBlocking { execute(any()) }.doReturn(
                GetResourceTypeWithFieldsByIdUseCase.Output(
                    resourceTypeId = RESOURCE_MODEL.resourceTypeId,
                    listOf(ResourceField(0, "description", false, null, false, "string"))
                )
            )
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)

        verify(view, times(2)).showDescription(DESCRIPTION, useSecretFont = false)
    }

    @Test
    fun `password details description encryption state should be shown correct for default password`() {
        mockGetResourceTypeWithFields.stub {
            onBlocking { execute(any()) }.doReturn(
                GetResourceTypeWithFieldsByIdUseCase.Output(
                    resourceTypeId = RESOURCE_MODEL.resourceTypeId,
                    listOf(ResourceField(1, name = "description", isSecret = true, null, false, "string"))
                )
            )
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)

        verify(view, times(2)).showDescriptionIsEncrypted()
    }

    @Test
    fun `eye icon should react to password visibility change correct`() {
        mockResourceTypeFactory.stub {
            onBlocking { getResourceTypeEnum(any()) }.doReturn(PASSWORD_WITH_DESCRIPTION)
        }
        val password = "pass"
        mockSecretPropertiesActionsInteractor.stub {
            onBlocking { providePassword() } doReturn flowOf(
                SecretPropertyActionResult.Success(
                    SecretPropertiesActionsInteractor.SECRET_LABEL,
                    isSecret = true,
                    password
                )
            )
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)
        presenter.secretIconClick()
        presenter.secretIconClick()

        verify(view).showPasswordVisibleIcon()
        verify(view).showPassword(password)
        verify(view, times(3)).showPasswordHiddenIcon()
        verify(view, times(3)).showPasswordHidden()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `view should show decrypt error correct`() = runTest {
        mockSecretPropertiesActionsInteractor.stub {
            onBlocking { providePassword() } doReturn flowOf(SecretPropertyActionResult.DecryptionFailure())
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)
        presenter.secretIconClick()

        verify(view).showDecryptionFailure()
    }

    @Test
    fun `view should show fetch error correct`() {
        mockSecretPropertiesActionsInteractor.stub {
            onBlocking { providePassword() } doReturn flowOf(SecretPropertyActionResult.FetchFailure())
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)
        presenter.secretIconClick()

        verify(view).showFetchFailure()
    }

    @Test
    fun `view should hide preview password when appropriate feature flag is set`() {
        mockGetFeatureFlagsUseCase.stub {
            onBlocking { execute(Unit) }.doReturn(
                GetFeatureFlagsUseCase.Output(
                    FeatureFlagsModel(
                        null,
                        null,
                        isPreviewPasswordAvailable = false,
                        areFoldersAvailable = false,
                        areTagsAvailable = false,
                        isTotpAvailable = true
                    )
                )
            )
        }

        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)

        verify(view, times(2)).hidePasswordEyeIcon()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `resource permissions should be displayed`() = runTest {
        presenter.argsReceived(
            RESOURCE_MODEL.resourceId,
            100,
            20f
        )
        presenter.resume(view)

        verify(view, times(2)).showPermissions(eq(listOf(groupPermission)), eq(listOf(userPermission)), any(), any())
    }

    private companion object {
        private const val NAME = "name"
        private const val USERNAME = "username"
        private const val INITIALS = "NN"
        private const val URL = "https://www.passbolt.com"
        private val ID = UUID.randomUUID().toString()
        private const val DESCRIPTION = "desc"
        private val RESOURCE_TYPE_ID = UUID.randomUUID().toString()
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
        private val DECRYPTED_SECRET = "decrypted".toByteArray()
        private val groupPermission = PermissionModelUi.GroupPermissionModel(
            ResourcePermission.READ, "permId1", GroupModel("grId", "grName")
        )
        private val userPermission = PermissionModelUi.UserPermissionModel(
            ResourcePermission.OWNER,
            "permId2",
            UserWithAvatar("usId", "first", "last", "uName", null)
        )
        private val RESOURCE_TAGS = listOf(
            TagModel("id1", "tag1", false),
            TagModel("id2", "tag2", false)
        )
    }
}
