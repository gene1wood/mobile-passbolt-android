package com.passbolt.mobile.android.feature.home.screen.recycler

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.passbolt.mobile.android.common.extension.DebounceClickEventHook
import com.passbolt.mobile.android.common.extension.asBinding
import com.passbolt.mobile.android.common.px
import com.passbolt.mobile.android.feature.home.R
import com.passbolt.mobile.android.feature.home.databinding.ItemPasswordBinding
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
class PasswordItem(
    val resourceModel: ResourceModel,
    private val dotsVisible: Boolean = true
) : AbstractBindingItem<ItemPasswordBinding>() {

    override val type: Int
        get() = R.id.itemPassword

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemPasswordBinding {
        return ItemPasswordBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemPasswordBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        with(binding) {
            setupUsername(this)
            title.text = resourceModel.name
            more.isVisible = dotsVisible
            loader.isVisible = resourceModel.loaderVisible
            itemPassword.isEnabled = resourceModel.clickable
            val initialsIcons = getInitialsIcon(binding.root.context)
            icon.setImageDrawable(initialsIcons)

            resourceModel.icon?.let {
                icon.load(it) {
                    placeholder(initialsIcons)
                }
            }
        }
    }

    private fun setupUsername(binding: ItemPasswordBinding) = with(binding) {
        val fontFamily = ResourcesCompat.getFont(binding.root.context, R.font.inter)

        if (resourceModel.username.isNullOrBlank()) {
            subtitle.typeface = Typeface.create(fontFamily, FONT_WEIGHT, true)
            subtitle.text = binding.root.context.getString(R.string.no_username)
        } else {
            subtitle.typeface = Typeface.create(fontFamily, FONT_WEIGHT, false)
            subtitle.text = resourceModel.username
        }
    }

    private fun getInitialsIcon(context: Context): Drawable {
        val generator = ColorGenerator.MATERIAL
        val generatedColor = generator.getColor(resourceModel.name)
        val color = ColorUtils.blendARGB(generatedColor, Color.WHITE, LIGHT_RATIO)
        return TextDrawable.builder()
            .beginConfig()
            .textColor(ColorUtils.blendARGB(color, generatedColor, DARK_RATIO))
            .useFont(ResourcesCompat.getFont(context, R.font.inter_medium))
            .endConfig()
            .buildRoundRect(resourceModel.initials, color, ICON_RADIUS)
    }

    class MoreClick(
        private val clickListener: (ResourceModel) -> Unit
    ) : DebounceClickEventHook<PasswordItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.asBinding<ItemPasswordBinding> {
                it.more
            }
        }

        override fun onDebounceClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<PasswordItem>,
            item: PasswordItem
        ) {
            clickListener.invoke(item.resourceModel)
        }
    }

    class ItemClick(
        private val clickListener: (ResourceModel) -> Unit
    ) : ClickEventHook<PasswordItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.asBinding<ItemPasswordBinding> {
                it.itemPassword
            }
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<PasswordItem>,
            item: PasswordItem
        ) {
            clickListener.invoke(item.resourceModel)
        }
    }

    companion object {
        private const val LIGHT_RATIO = 0.8f
        private const val DARK_RATIO = 0.95f
        private const val FONT_WEIGHT = 400
        private val ICON_RADIUS = 4.px
    }
}
