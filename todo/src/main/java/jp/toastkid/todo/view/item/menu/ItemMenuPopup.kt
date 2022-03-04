/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.item.menu

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import jp.toastkid.todo.R
import jp.toastkid.todo.model.TodoTask

/**
 *
 * @param context Use for obtaining [PopupWindow]
 * @param action [ItemMenuPopupActionUseCase]
 * @author toastkidjp
 */
class ItemMenuPopup(
    context: Context,
    private val action: ItemMenuPopupActionUseCase,
    view: ItemMenuPopupView = ItemMenuViewImplementation(context)
) {

    private val popupWindow = PopupWindow(context)

    private var lastTask: TodoTask? = null

    init {
        popupWindow.contentView = view.getView()
        popupWindow.isOutsideTouchable = true
        popupWindow.width = context.resources.getDimensionPixelSize(WIDTH)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        view.setPopup(this)
    }

    fun show(view: View, item: TodoTask) {
        lastTask = item
        popupWindow.showAsDropDown(view)
    }

    fun modify() {
        lastTask?.let {
            action.modify(it)
        }
        popupWindow.dismiss()
    }

    fun delete() {
        lastTask?.let {
            action.delete(it)
        }
        popupWindow.dismiss()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.popup_todo_tasks_item_menu

        @DimenRes
        private val WIDTH = R.dimen.item_menu_popup_width

    }
}