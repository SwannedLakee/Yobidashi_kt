/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.api.trend

import okhttp3.ResponseBody
import retrofit2.Converter

/**
 * @author toastkidjp
 */
class TrendResponseConverter(private val parser: TrendParser) : Converter<ResponseBody, List<Trend>> {

    override fun convert(responseBody: ResponseBody): List<Trend> {
        val bodyString = responseBody.use { it.string() }
        return parser(bodyString)
    }
}