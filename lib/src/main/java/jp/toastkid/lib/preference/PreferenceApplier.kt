package jp.toastkid.lib.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.ColorInt
import jp.toastkid.lib.Urls
import java.io.File
import java.util.Locale
import kotlin.math.max

/**
 * Preferences wrapper.

 * @author toastkidjp
 */
class PreferenceApplier(private val context: Context) {

    @SuppressWarnings("unused")
    @Deprecated("These keys are deprecated.")
    private enum class DefunctKey {
        USE_DAILY_ALARM, USE_INTERNAL_BROWSER, MENU_POS, USE_INVERSION, ENABLE_APP_SEARCH,
        LAST_AD_DATE, FULL_SCREEN
    }

    private enum class Key {
        BG_COLOR, FONT_COLOR,
        ENABLE_SUGGESTION, ENABLE_SEARCH_HISTORY, ENABLE_VIEW_HISTORY, ENABLE_URL_MODULE,
        ENABLE_TREND_MODULE, ENABLE_FAVORITE_SEARCH, DISABLE_SEARCH_CATEGORIES,
        BG_IMAGE,
        USE_NOTIFICATION_WIDGET, USE_DAILY_NOTIFICATION, RETAIN_TABS, USE_JS,
        LOAD_IMAGE, SAVE_FORM, USER_AGENT, HOME_URL, USE_COLOR_FILTER, FILTER_COLOR,
        DEFAULT_SEARCH_ENGINE, ENABLE_SEARCH_QUERY_EXTRACT, ENABLE_SEARCH_WITH_CLIP, START_UP, SAVE_VIEW_HISTORY,
        SCREEN_MODE, WIFI_ONLY_MODE, AD_REMOVE, WEB_VIEW_POOL_SIZE,
        EDITOR_BACKGROUND_COLOR, EDITOR_FONT_COLOR, EDITOR_CURSOR_COLOR, EDITOR_HIGHLIGHT_COLOR,
        EDITOR_FONT_SIZE, CAMERA_FAB_BUTTON_POSITION_X, CAMERA_FAB_BUTTON_POSITION_Y,
        MENU_FAB_BUTTON_POSITION_X, MENU_FAB_BUTTON_POSITION_Y,
        WEB_VIEW_BACKGROUND_ALPHA, RSS_READER_TARGETS, IMAGE_VIEWER_EXCLUDED_PATHS,
        IMAGE_VIEWER_SORT_TYPE, BROWSER_DARK_MODE, USE_TITLE_FILTER,
        ARTICLE_LIST_SORT_TYPE
    }

    private val preferences: SharedPreferences =
            context.getSharedPreferences(javaClass.canonicalName, Context.MODE_PRIVATE)

    var color: Int
        get() = preferences.getInt(Key.BG_COLOR.name, Color.BLUE)
        set(color) = preferences.edit().putInt(Key.BG_COLOR.name, color).apply()

    var fontColor: Int
        get() = preferences.getInt(Key.FONT_COLOR.name, Color.WHITE)
        set(color) = preferences.edit().putInt(Key.FONT_COLOR.name, color).apply()

    fun colorPair(): ColorPair = ColorPair(color, fontColor)

    val isEnableSuggestion: Boolean
        get() = preferences.getBoolean(Key.ENABLE_SUGGESTION.name, true)

    val isDisableSuggestion: Boolean
        get() = !isEnableSuggestion

    fun switchEnableSuggestion() {
        preferences.edit().putBoolean(Key.ENABLE_SUGGESTION.name, !isEnableSuggestion).apply()
    }

    val isEnableSearchHistory: Boolean
        get() = preferences.getBoolean(Key.ENABLE_SEARCH_HISTORY.name, true)

    fun switchEnableSearchHistory() {
        preferences.edit().putBoolean(Key.ENABLE_SEARCH_HISTORY.name, !isEnableSearchHistory)
                .apply()
    }

    val isEnableFavoriteSearch: Boolean
        get() = preferences.getBoolean(Key.ENABLE_FAVORITE_SEARCH.name, true)

    fun switchEnableFavoriteSearch() {
        preferences.edit().putBoolean(Key.ENABLE_FAVORITE_SEARCH.name, !isEnableFavoriteSearch)
                .apply()
    }

    val isEnableViewHistory: Boolean
        get() = preferences.getBoolean(Key.ENABLE_VIEW_HISTORY.name, true)

    fun switchEnableViewHistory() {
        preferences.edit().putBoolean(Key.ENABLE_VIEW_HISTORY.name, !isEnableViewHistory)
                .apply()
    }

    fun isEnableUrlModule(): Boolean {
        return preferences.getBoolean(Key.ENABLE_URL_MODULE.name, true)
    }

    fun switchEnableUrlModule() {
        preferences.edit().putBoolean(Key.ENABLE_URL_MODULE.name, !isEnableUrlModule())
                .apply()
    }

    fun isEnableTrendModule(): Boolean {
        return preferences.getBoolean(Key.ENABLE_TREND_MODULE.name, true)
    }

    fun switchEnableTrendModule() {
        preferences.edit().putBoolean(Key.ENABLE_TREND_MODULE.name, !isEnableTrendModule())
                .apply()
    }

    fun addDisableSearchCategory(name: String) {
        val set = readDisableSearchCategory()
        set?.add(name)
        preferences.edit().putStringSet(Key.DISABLE_SEARCH_CATEGORIES.name, set).apply()
    }

    fun removeDisableSearchCategory(name: String) {
        val set = readDisableSearchCategory()
        set?.remove(name)
        preferences.edit().putStringSet(Key.DISABLE_SEARCH_CATEGORIES.name, set).apply()
    }

    fun readDisableSearchCategory(): MutableSet<String>? {
        return preferences.getStringSet(Key.DISABLE_SEARCH_CATEGORIES.name, mutableSetOf())
    }

    var backgroundImagePath: String
        get() = preferences.getString(Key.BG_IMAGE.name, "") ?: ""
        set(path) = preferences.edit().putString(Key.BG_IMAGE.name, path).apply()

    fun removeBackgroundImagePath() {
        preferences.edit().remove(Key.BG_IMAGE.name).apply()
    }

    val isFirstLaunch: Boolean
        get() {
            val firstLaunch = File(context.filesDir, "firstLaunch")
            if (firstLaunch.exists()) {
                return false
            }
            firstLaunch.mkdirs()
            return true
        }

    fun setUseNotificationWidget(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_NOTIFICATION_WIDGET.name, newState).apply()
    }

    fun useNotificationWidget(): Boolean =
            preferences.getBoolean(Key.USE_NOTIFICATION_WIDGET.name, false)

    fun setUseDailyNotification(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_DAILY_NOTIFICATION.name, newState).apply()
    }

    fun useDailyNotification(): Boolean =
            preferences.getBoolean(Key.USE_DAILY_NOTIFICATION.name, false)

    fun setRetainTabs(newState: Boolean) {
        preferences.edit().putBoolean(Key.RETAIN_TABS.name, newState).apply()
    }

    fun doesRetainTabs(): Boolean = preferences.getBoolean(Key.RETAIN_TABS.name, true)

    fun setUseJavaScript(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_JS.name, newState).apply()
    }

    fun useJavaScript(): Boolean = preferences.getBoolean(Key.USE_JS.name, true)

    fun setLoadImage(newState: Boolean) {
        preferences.edit().putBoolean(Key.LOAD_IMAGE.name, newState).apply()
    }

    fun doesLoadImage(): Boolean = preferences.getBoolean(Key.LOAD_IMAGE.name, true)

    fun setSaveForm(newState: Boolean) {
        preferences.edit().putBoolean(Key.SAVE_FORM.name, newState).apply()
    }

    fun doesSaveForm(): Boolean = preferences.getBoolean(Key.SAVE_FORM.name, false)

    fun setUserAgent(path: String) {
        preferences.edit().putString(Key.USER_AGENT.name, path).apply()
    }

    fun userAgent(): String = preferences.getString(Key.USER_AGENT.name, "DEFAULT") ?: ""

    var homeUrl: String
        get() = preferences.getString(Key.HOME_URL.name,
                if (Locale.getDefault().language == Locale.ENGLISH.language) {
                    "https://www.yahoo.com"
                } else {
                    "https://m.yahoo.co.jp"
                }
        ) ?: ""
        set(path) {
            if (Urls.isInvalidUrl(path)) {
                return
            }
            preferences.edit().putString(Key.HOME_URL.name, path).apply()
        }

    fun setUseColorFilter(newState: Boolean) {
        preferences.edit().putBoolean(Key.USE_COLOR_FILTER.name, newState).apply()
    }

    fun useColorFilter(): Boolean = preferences.getBoolean(Key.USE_COLOR_FILTER.name, false)

    fun setFilterColor(@ColorInt newState: Int) {
        preferences.edit().putInt(Key.FILTER_COLOR.name, newState).apply()
    }

    @ColorInt fun filterColor(substitute: Int): Int =
            preferences.getInt(
                    Key.FILTER_COLOR.name,
                    substitute
            )

    fun setDefaultSearchEngine(category: String) {
        preferences.edit().putString(Key.DEFAULT_SEARCH_ENGINE.name, category).apply()
    }

    fun getDefaultSearchEngine(): String? {
        return preferences.getString(
                Key.DEFAULT_SEARCH_ENGINE.name,
                ""
        )
    }

    var enableSearchQueryExtract: Boolean
        get () = preferences.getBoolean(Key.ENABLE_SEARCH_WITH_CLIP.name, true)
        set (newState) {
            preferences.edit().putBoolean(Key.ENABLE_SEARCH_WITH_CLIP.name, newState).apply()
        }

    var enableSearchWithClip: Boolean
        get () = preferences.getBoolean(Key.ENABLE_SEARCH_QUERY_EXTRACT.name, true)
        set (newState) {
            preferences.edit().putBoolean(Key.ENABLE_SEARCH_QUERY_EXTRACT.name, newState).apply()
        }

    var startUp: String?
        get () = preferences.getString(Key.START_UP.name, "")
        set (newValue) = preferences.edit().putString(Key.START_UP.name, newValue).apply()

    var saveViewHistory: Boolean
        get () = preferences.getBoolean(Key.SAVE_VIEW_HISTORY.name, true)
        set (newState) = preferences.edit().putBoolean(Key.SAVE_VIEW_HISTORY.name, newState).apply()

    fun setBrowserScreenMode(newState: String) {
        preferences.edit().putString(Key.SCREEN_MODE.name, newState).apply()
    }

    fun browserScreenMode(): String? = preferences.getString(Key.SCREEN_MODE.name, "")

    var wifiOnly: Boolean
        get () = preferences.getBoolean(Key.WIFI_ONLY_MODE.name, true)
        set (newValue) = preferences.edit().putBoolean(Key.WIFI_ONLY_MODE.name, newValue).apply()

    var adRemove: Boolean
        get () = preferences.getBoolean(Key.AD_REMOVE.name, true)
        set (newValue) = preferences.edit().putBoolean(Key.AD_REMOVE.name, newValue).apply()

    var poolSize: Int
        get () = preferences.getInt(Key.WEB_VIEW_POOL_SIZE.name, 6)
        set (newValue) = preferences.edit().putInt(Key.WEB_VIEW_POOL_SIZE.name, newValue).apply()

    fun setEditorBackgroundColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_BACKGROUND_COLOR.name, newValue).apply()
    }

    fun editorBackgroundColor(): Int {
        return preferences.getInt(Key.EDITOR_BACKGROUND_COLOR.name, Color.WHITE)
    }

    fun setEditorFontColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_FONT_COLOR.name, newValue).apply()
    }

    fun editorFontColor(): Int {
        return preferences.getInt(Key.EDITOR_FONT_COLOR.name, Color.BLACK)
    }

    fun setEditorCursorColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_CURSOR_COLOR.name, newValue).apply()
    }

    @ColorInt
    fun editorCursorColor(@ColorInt substitute: Int): Int {
        return preferences.getInt(Key.EDITOR_CURSOR_COLOR.name, substitute)
    }

    fun setEditorHighlightColor(@ColorInt newValue: Int) {
        preferences.edit().putInt(Key.EDITOR_HIGHLIGHT_COLOR.name, newValue).apply()
    }

    @ColorInt
    fun editorHighlightColor(@ColorInt substitute: Int): Int {
        return preferences.getInt(Key.EDITOR_HIGHLIGHT_COLOR.name, substitute)
    }

    fun setEditorFontSize(newSize: Int) {
        preferences.edit().putInt(Key.EDITOR_FONT_SIZE.name, newSize).apply()
    }

    fun editorFontSize(): Int {
        return preferences.getInt(Key.EDITOR_FONT_SIZE.name, 16)
    }

    fun setNewCameraFabPosition(x: Float, y: Float) {
        preferences.edit()
                .putFloat(Key.CAMERA_FAB_BUTTON_POSITION_X.name, x)
                .putFloat(Key.CAMERA_FAB_BUTTON_POSITION_Y.name, y)
                .apply()
    }

    fun clearCameraFabPosition() {
        preferences.edit()
                .remove(Key.CAMERA_FAB_BUTTON_POSITION_X.name)
                .remove(Key.CAMERA_FAB_BUTTON_POSITION_Y.name)
                .apply()
    }

    fun cameraFabPosition(): Pair<Float, Float>? {
        if (!preferences.contains(Key.CAMERA_FAB_BUTTON_POSITION_X.name)
                || !preferences.contains(Key.CAMERA_FAB_BUTTON_POSITION_Y.name)) {
            return null
        }
        return preferences.getFloat(Key.CAMERA_FAB_BUTTON_POSITION_X.name, -1f) to
                preferences.getFloat(Key.CAMERA_FAB_BUTTON_POSITION_Y.name, -1f)
    }


    fun setNewMenuFabPosition(x: Float, y: Float) {
        preferences.edit()
                .putFloat(Key.MENU_FAB_BUTTON_POSITION_X.name, max(0f, x))
                .putFloat(Key.MENU_FAB_BUTTON_POSITION_Y.name, max(0f, y))
                .apply()
    }

    fun clearMenuFabPosition() {
        preferences.edit()
                .remove(Key.MENU_FAB_BUTTON_POSITION_X.name)
                .remove(Key.MENU_FAB_BUTTON_POSITION_Y.name)
                .apply()
    }

    fun menuFabPosition(): Pair<Float, Float>? {
        if (!preferences.contains(Key.MENU_FAB_BUTTON_POSITION_X.name)
                || !preferences.contains(Key.MENU_FAB_BUTTON_POSITION_Y.name)) {
            return null
        }
        return preferences.getFloat(Key.MENU_FAB_BUTTON_POSITION_X.name, -1f) to
                preferences.getFloat(Key.MENU_FAB_BUTTON_POSITION_Y.name, -1f)
    }

    fun setWebViewBackgroundAlpha(alpha: Float) {
        preferences.edit().putFloat(Key.WEB_VIEW_BACKGROUND_ALPHA.name, alpha).apply()
    }

    fun getWebViewBackgroundAlpha(): Float {
        return preferences.getFloat(Key.WEB_VIEW_BACKGROUND_ALPHA.name, 1f)
    }

    fun readRssReaderTargets(): MutableSet<String> {
        return preferences.getStringSet(Key.RSS_READER_TARGETS.name, mutableSetOf())
                ?: mutableSetOf()
    }

    fun saveNewRssReaderTargets(url: String) {
        val targets = readRssReaderTargets()
        targets.add(url)
        preferences.edit().putStringSet(Key.RSS_READER_TARGETS.name, targets).apply()
    }

    fun removeFromRssReaderTargets(url: String) {
        val targets = readRssReaderTargets()
        targets.remove(url)
        preferences.edit().putStringSet(Key.RSS_READER_TARGETS.name, targets).apply()
    }

    fun containsRssTarget(url: String) = readRssReaderTargets().contains(url)

    fun clear() {
        preferences.edit().clear().apply()
    }

    fun addExcludeItem(path: String) {
        preferences.edit().putStringSet(
                Key.IMAGE_VIEWER_EXCLUDED_PATHS.name,
                mutableSetOf(path).also { it.addAll(excludedItems()) }
        )
                .apply()
    }

    fun excludedItems(): Set<String> =
            preferences.getStringSet(Key.IMAGE_VIEWER_EXCLUDED_PATHS.name, emptySet()) ?: emptySet()

    fun removeFromExcluding(path: String) {
        mutableSetOf<String>().also {
            it.addAll(excludedItems())
            it.remove(path)
            preferences
                    .edit()
                    .putStringSet(Key.IMAGE_VIEWER_EXCLUDED_PATHS.name, it)
                    .apply()
        }
    }

    fun imageViewerSort(): String? {
        return preferences.getString(Key.IMAGE_VIEWER_SORT_TYPE.name, null)
    }

    fun setImageViewerSort(sort: String) {
        preferences.edit().putString(Key.IMAGE_VIEWER_SORT_TYPE.name, sort).apply()
    }

    fun useDarkMode(): Boolean {
        return preferences.getBoolean(
                Key.BROWSER_DARK_MODE.name,
                (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        )
    }

    fun setUseDarkMode(newState: Boolean) {
        preferences.edit().putBoolean(Key.BROWSER_DARK_MODE.name, newState).apply()
    }

    fun switchUseTitleFilter(checked: Boolean) {
        preferences.edit().putBoolean(Key.USE_TITLE_FILTER.name, checked).apply()
    }

    fun useTitleFilter(): Boolean {
        return preferences.getBoolean(Key.USE_TITLE_FILTER.name, false)
    }

    fun setArticleSort(name: String) {
        preferences.edit().putString(Key.ARTICLE_LIST_SORT_TYPE.name, name).apply()
    }

    fun articleSort(): String {
        return preferences.getString(Key.ARTICLE_LIST_SORT_TYPE.name, "") ?: ""
    }
}
