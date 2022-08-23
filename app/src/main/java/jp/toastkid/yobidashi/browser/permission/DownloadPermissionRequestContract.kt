/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class DownloadPermissionRequestContract : ActivityResultContract<String?, Pair<Boolean, String?>>() {

    private var url: String? = null

    override fun createIntent(context: Context, input: String?): Intent {
        url = input

        val permissions =
            if (Build.VERSION.SDK_INT >= 33) {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        return Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS)
            .putExtra(
                ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS,
                permissions
            )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, String?> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true to url
        }
        if (resultCode != Activity.RESULT_OK) return false to url
        val granted = intent
            ?.getIntArrayExtra(ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS)
            ?.getOrNull(0) == PackageManager.PERMISSION_GRANTED
        return granted to url
    }

}