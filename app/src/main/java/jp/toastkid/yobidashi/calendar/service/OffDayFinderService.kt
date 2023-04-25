/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.service

interface OffDayFinderService {

    operator fun invoke(
        year: Int,
        month: Int,
        date: Int,
        dayOfWeek: Int,
        useUserOffDay: Boolean = true
    ): Boolean

}