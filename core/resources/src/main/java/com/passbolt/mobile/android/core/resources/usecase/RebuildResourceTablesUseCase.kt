package com.passbolt.mobile.android.core.resources.usecase

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.database.impl.resources.AddLocalResourcesUseCase
import com.passbolt.mobile.android.database.impl.resources.RemoveLocalResourcesUseCase
import com.passbolt.mobile.android.storage.usecase.input.UserIdInput
import com.passbolt.mobile.android.storage.usecase.selectedaccount.GetSelectedAccountUseCase
import com.passbolt.mobile.android.ui.ResourceModel

class RebuildResourceTablesUseCase(
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase,
    private val removeLocalResourcesUseCase: RemoveLocalResourcesUseCase,
    private val addLocalResourcesUseCase: AddLocalResourcesUseCase
) : AsyncUseCase<RebuildResourceTablesUseCase.Input, Unit> {

    override suspend fun execute(input: Input) {
        val selectedAccount = requireNotNull(getSelectedAccountUseCase.execute(Unit).selectedAccount)
        removeLocalResourcesUseCase.execute(UserIdInput(selectedAccount))
        addLocalResourcesUseCase.execute(AddLocalResourcesUseCase.Input(input.resources, selectedAccount))
    }

    data class Input(
        val resources: List<ResourceModel>
    )
}
