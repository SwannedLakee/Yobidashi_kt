/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import jp.toastkid.yobidashi.libs.db.AppDatabase

class DatabaseFinder {

    fun invoke(context: Context): AppDatabase {
        synchronized(DatabaseFinder) {
            if (instance != null) {
                return@synchronized
            }

            instance = makeAppDatabase(context)
        }

        return instance?: makeAppDatabase(context)
    }

    private fun makeAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_FILE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        private const val DATABASE_FILE_NAME = "yobidashi2.db"

        private var instance: AppDatabase? = null

        /**
         * For reset instance each test.
         */
        @VisibleForTesting
        fun clearInstance() {
            instance = null
        }

    }

}