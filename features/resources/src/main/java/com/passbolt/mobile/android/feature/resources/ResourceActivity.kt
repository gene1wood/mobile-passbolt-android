package com.passbolt.mobile.android.feature.resources

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.passbolt.mobile.android.common.lifecycleawarelazy.lifecycleAwareLazy
import com.passbolt.mobile.android.core.extension.findNavHostFragment
import com.passbolt.mobile.android.core.mvp.viewbinding.BindingActivity
import com.passbolt.mobile.android.core.security.flagsecure.FlagSecureSetter
import com.passbolt.mobile.android.feature.resources.databinding.ActivityResourcesBinding
import com.passbolt.mobile.android.ui.ResourceModel
import org.koin.android.ext.android.inject

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
class ResourceActivity : BindingActivity<ActivityResourcesBinding>(ActivityResourcesBinding::inflate) {

    private val flagSecureSetter: FlagSecureSetter by inject()
    private val mode by lifecycleAwareLazy {
        intent.getSerializableExtra(EXTRA_RESOURCE_MODE) as ResourceMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flagSecureSetter.set(this)

        val navHostFragment = findNavHostFragment(R.id.fragmentContainer)
        val inflater = navHostFragment.navController.navInflater

        val graph = when (mode) {
            ResourceMode.NEW -> inflater.inflate(R.navigation.resources_new)
            ResourceMode.EDIT -> inflater.inflate(R.navigation.resources_new)
            ResourceMode.DETAILS -> inflater.inflate(R.navigation.resources_details)
        }

        navHostFragment.navController.setGraph(graph, intent.extras)
    }

    companion object {
        const val RESULT_RESOURCE_DELETED = 8000
        const val RESULT_RESOURCE_EDITED = 8001
        const val RESULT_RESOURCE_CREATED = 8002
        const val EXTRA_RESOURCE_MODEL = "RESOURCE_MODEL"
        const val EXTRA_RESOURCE_MODE = "RESOURCE_MODE"

        const val EXTRA_RESOURCE_NAME = "RESOURCE_NAME"
        const val EXTRA_RESOURCE_ID = "RESOURCE_ID"

        fun newInstance(context: Context, mode: ResourceMode, existingResource: ResourceModel? = null) =
            Intent(context, ResourceActivity::class.java).apply {
                putExtra(EXTRA_RESOURCE_MODE, mode)
                putExtra(EXTRA_RESOURCE_MODEL, existingResource)
            }

        fun resourceNameResultIntent(resourceName: String) =
            Intent().apply {
                putExtra(EXTRA_RESOURCE_NAME, resourceName)
            }

        fun resourceNameAndIdIntent(resourceName: String, resourceId: String) =
            Intent().apply {
                putExtra(EXTRA_RESOURCE_NAME, resourceName)
                putExtra(EXTRA_RESOURCE_ID, resourceId)
            }
    }
}
