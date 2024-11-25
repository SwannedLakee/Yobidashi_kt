/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.number.factory.GameFileProvider
import jp.toastkid.number.model.NumberBoard
import jp.toastkid.number.model.NumberPlaceGame
import jp.toastkid.number.presentation.state.CellState
import jp.toastkid.number.repository.GameRepositoryImplementation

class NumberPlaceViewModel {

    private val _game = mutableStateOf(NumberPlaceGame())

    private val _mask = mutableStateOf(NumberBoard())

    private val _loading = mutableStateOf(false)

    private val numberStates = mutableStateMapOf<String, CellState>()

    private val fontSize = 32.sp

    fun initialize(maskingCount: Int) {
        _loading.value = true
        _game.value.initialize(maskingCount)
        _mask.value = _game.value.masked()
        walkMatrix(_game.value.masked().rows()) { rowIndex, columnIndex ->
            numberStates.put(
                "${rowIndex}-${columnIndex}",
                CellState()
            )
        }
        _loading.value = false
    }

    fun initializeSolving() {
        _loading.value = true
        _game.value.initializeSolving()
        _mask.value = _game.value.masked()
        numberStates.keys.forEach { numberStates.put(it, CellState()) }
        _loading.value = false
    }

    fun setGame(game: NumberPlaceGame) {
        _loading.value = true
        _game.value = game
        _mask.value = _game.value.masked()
        walkMatrix(_game.value.masked().rows(), ::setSolving)
        _loading.value = false
    }

    fun setCorrect() {
        _loading.value = true
        _game.value.setCorrect()
        _mask.value = _game.value.masked()
        walkMatrix(_game.value.masked().rows(), ::setSolving)
        _loading.value = false
    }

    private fun walkMatrix(matrix: List<List<Int>>, biConsumer: (Int, Int) -> Unit) {
        matrix.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, i ->
                biConsumer(rowIndex, columnIndex)
            }
        }
    }

    fun masked() = _mask.value

    fun loading(): State<Boolean> = _loading

    private fun setSolving(rowIndex: Int, columnIndex: Int) {
        val solving = _game.value.pickSolving(rowIndex, columnIndex)
        numberStates.put("${rowIndex}-${columnIndex}", CellState(solving))
    }

    fun place(rowIndex: Int, columnIndex: Int, it: Int, onSolved: (Boolean) -> Unit) {
        _game.value.place(rowIndex, columnIndex, it, onSolved)
        numberStates.put("${rowIndex}-${columnIndex}", CellState(it))
    }

    fun useHint(
        rowIndex: Int,
        columnIndex: Int,
        onSolved: (Boolean) -> Unit
    ) {
        val it = _game.value.pickCorrect(rowIndex, columnIndex)
        place(rowIndex, columnIndex, it, onSolved)
    }

    fun saveCurrentGame(context: Context) {
        val preferenceApplier = PreferenceApplier(context)
        val file = GameFileProvider().invoke(context.filesDir, preferenceApplier) ?: return
        GameRepositoryImplementation().save(file, _game.value)
    }

    fun numberLabel(number: Int): String {
        return if (number == -1) "_" else "$number"
    }

    fun openingCellOption(rowIndex: Int, columnIndex: Int): Boolean {
        val state = numberStates.get("${rowIndex}-${columnIndex}") ?: return false
        return state.open
    }

    fun openCellOption(rowIndex: Int, columnIndex: Int) {
        val state = numberStates.get("${rowIndex}-${columnIndex}") ?: return
        numberStates.put("${rowIndex}-${columnIndex}", state.copy(open = true))
    }

    fun closeCellOption(rowIndex: Int, columnIndex: Int) {
        val state = numberStates.get("${rowIndex}-${columnIndex}") ?: return
        numberStates.put("${rowIndex}-${columnIndex}", state.copy(open = false))
    }

    fun numberLabel(rowIndex: Int, columnIndex: Int): String {
        val state = numberStates.get("${rowIndex}-${columnIndex}") ?: return ""
        return state.text()
    }

    fun fontSize() = fontSize

}