/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.view.refresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class SwipeRefreshNestedScrollConnection(
    private val state: SwipeRefreshState?,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {
    var enabled: Boolean = false
    var refreshTrigger: Float = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        // If we're refreshing, return zero
        state?.isRefreshing == true -> Offset.Zero
        // If the user is swiping up, handle it
        source == NestedScrollSource.Drag -> onScroll(available)
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (state?.isRefreshing == false && state.indicatorOffset >= refreshTrigger) {
            onRefresh()
            state.isSwipeInProgress = false
        } else {
            coroutineScope.launch {
                state?.resetOffset()
            }
        }
        return when {
            // If swiping isn't enabled, return zero
            !enabled -> Offset.Zero
            // If we're refreshing, return zero
            state?.isRefreshing == true -> Offset.Zero
            // If the user is swiping down and there's y remaining, handle it
            source == NestedScrollSource.Drag -> onScroll(available)
            else -> Offset.Zero
        }
    }

    private fun onScroll(available: Offset): Offset {
        if (available.y < 0) {
            state?.isSwipeInProgress = true
        } else if (state?.indicatorOffset?.roundToInt() == 0) {
            state.isSwipeInProgress = false
        }

        val dragConsumed = (available.y * -0.3f) - (state?.indicatorOffset ?: 0f)

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                state?.dispatchScrollDelta(dragConsumed)
            }
            // Return the consumed Y
            Offset(x = 0f, y = dragConsumed)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // If we're dragging, not currently refreshing and scrolled
        // past the trigger point, refresh!
        if (state?.isRefreshing != null && state.indicatorOffset >= refreshTrigger) {
            onRefresh()
        }

        // Reset the drag in progress state
        state?.isSwipeInProgress = false

        // Don't consume any velocity, to allow the scrolling layout to fling
        return Velocity.Zero
    }

}