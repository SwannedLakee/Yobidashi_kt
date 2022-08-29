/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.preview

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * @author toastkidjp
 */
class RotatedBitmapFactory(
        private val rotateMatrixFactory: RotateMatrixFactory = RotateMatrixFactory()
) {

    private fun applyMatrix(bitmap: Bitmap, matrix: Matrix) =
            Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    false
            )

    companion object {
        private val horizontalMatrix = Matrix().also { it.preScale(-1f, 1f) }
    }
}