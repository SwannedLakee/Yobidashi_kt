package jp.toastkid.yobidashi.libs

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * @author toastkidjp
 */
object Inputs {

    /**
     * Show software keyboard.
     *
     * @param activity
     * @param editText
     */
    fun showKeyboard(activity: Activity, editText: EditText) {
        val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (inputMethodManager?.isActive == false) {
            return
        }
        inputMethodManager?.showSoftInput(editText, 0)
    }

    /**
     * Hide software keyboard.
     *
     * @param v
     */
    fun hideKeyboard(v: View?) {
        val manager = v?.context
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        if (!manager.isActive) {
            return
        }
        manager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    /**
     * Show software keyboard for input dialog.
     * You should call this method from `onActivityCreated(savedInstanceState: Bundle?)`.
     *
     * @param window Nullable [Window] for calling setSoftInputMode.
     */
    fun showKeyboardForInputDialog(window: Window?) {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}
