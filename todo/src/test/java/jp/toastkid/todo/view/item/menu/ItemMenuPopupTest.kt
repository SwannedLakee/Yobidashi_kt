/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.item.menu

import android.content.Context
import android.widget.PopupWindow
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify

class ItemMenuPopupTest {

    private lateinit var itemMenuPopup: ItemMenuPopup

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var action: ItemMenuPopupActionUseCase

    @MockK
    private lateinit var view: ItemMenuPopupView

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources.getDimensionPixelSize(any()) }.returns(1)
        every { action.modify(any()) }.just(Runs)
        every { action.delete(any()) }.just(Runs)
        every { view.setPopup(any()) }.just(Runs)
        every { view.getView() }.returns(mockk())

        mockkConstructor(PopupWindow::class)
        every { anyConstructed<PopupWindow>().contentView = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().isOutsideTouchable = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().width = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().height = any() }.just(Runs)
        every { anyConstructed<PopupWindow>().showAsDropDown(any()) }.just(Runs)
        every { anyConstructed<PopupWindow>().dismiss() }.just(Runs)

        itemMenuPopup = ItemMenuPopup(context, action, view)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun show() {
        itemMenuPopup.show(mockk(), mockk())

        verify { anyConstructed<PopupWindow>().showAsDropDown(any()) }
    }
}