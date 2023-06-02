package jp.toastkid.yobidashi.tab

import android.content.Context
import androidx.annotation.Keep
import jp.toastkid.yobidashi.browser.archive.IdGenerator
import jp.toastkid.yobidashi.tab.model.ArticleListTab
import jp.toastkid.yobidashi.tab.model.ArticleTab
import jp.toastkid.yobidashi.tab.model.CalendarTab
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * First class collection of [Tab].
 *
 * @author toastkidjp
 */
@Serializable
class TabList {

    @Transient
    private val tabs: MutableList<Tab> = mutableListOf()

    @Required
    @Keep
    private var index: Int = 0

    internal fun currentTab(): Tab? {
        if (tabs.isEmpty() || invalidIndex(index)) {
            return null
        }
        return tabs[index]
    }

    internal fun setIndex(newIndex: Int) {
        index = if (invalidIndex(newIndex)) 0 else newIndex
    }

    @Keep
    fun getIndex(): Int = index

    @Keep
    fun size(): Int = tabs.size

    internal fun get(position: Int): Tab? =
            if (position < 0 || tabs.size <= position) {
                null
            } else {
                tabs[position]
            }

    internal fun set(index: Int, currentTab: Tab) {
        val target = if (invalidIndex(index)) 0 else index
        tabs[target] = currentTab
    }

    private fun invalidIndex(newIndex: Int): Boolean {
        return !inRange(newIndex)
    }

    /**
     * Save current state to file.
     */
    internal fun save() {
        val json = jsonSerializer.encodeToString(this)
        tabsFile?.let {
            it.sink().use {  sink ->
                sink.buffer().use { buffered ->
                    buffered.writeUtf8(json)
                    buffered.flush()
                }
            }
        }
        savingLock.withLock {
            itemsDir?.let {
                it.deleteRecursively()
                it.mkdirs()
            }
            tabs.forEach { tab ->
                val source: ByteArray? = when (tab) {
                    is WebTab -> jsonSerializer.encodeToString(tab)?.toByteArray(charset)
                    is EditorTab -> jsonSerializer.encodeToString(tab)?.toByteArray(charset)
                    is PdfTab -> jsonSerializer.encodeToString(tab)?.toByteArray(charset)
                    is ArticleTab -> jsonSerializer.encodeToString(tab)?.toByteArray(charset)
                    is ArticleListTab -> jsonSerializer.encodeToString(tab)?.toByteArray(charset)
                    is CalendarTab -> jsonSerializer.encodeToString(tab)?.toByteArray(charset)
                    else -> ByteArray(0)
                }
                source?.let {
                    File(itemsDir, "${tab.id()}.json").sink().use {  sink ->
                        sink.buffer().use {  bufferedSink ->
                            bufferedSink.write(source)
                        }
                    }
                }
            }
        }
    }

    internal val isEmpty: Boolean
        get() = tabs.isEmpty()

    internal fun add(newTab: Tab) {
        addTo(newTab, index + 1)
    }

    internal fun addTo(tab: Tab, index: Int) {
        if (inRange(index)) {
            tabs.add(index, tab)
        } else {
            tabs.add(tab)
        }
    }

    internal fun closeTab(index: Int) {
        if (index <= this.index && this.index != 0) {
            this.index--
        }
        val tab: Tab = tabs[index]
        File(itemsDir, tab.id() + ".json").delete()
        tabs.remove(tab)
    }

    internal fun clear() {
        tabs.clear()
        index = 0
        tabsFile?.delete()
        itemsDir?.delete()
        save()
    }

    fun swap(from: Int, to: Int) {
        if (inRange(from, to)) {
            val currentTab = currentTab() ?: return
            Collections.swap(tabs, from, to)
            setIndex(tabs.indexOf(currentTab))
        }
    }

    private fun inRange(vararg indexes: Int): Boolean {
        val size = tabs.size
        return indexes.none { it < 0 || size <= it }
    }

    companion object {

        private const val TABS_DIR = "tabs"

        private const val TABS_ITEM_DIR = "$TABS_DIR/items"

        private val savingLock = ReentrantLock()

        private var tabsFile: File? = null

        private val charset = charset("UTF-8")

        private val jsonSerializer = Json {
            ignoreUnknownKeys = true
        }

        private var itemsDir: File? = null

        internal fun loadOrInit(context: Context): TabList {
            initTabsFile(context)
            if (tabsFile == null || tabsFile?.exists() == false) {
                return TabList()
            }

            try {
                val file = tabsFile
                val fromJson: TabList =
                        if (file == null) TabList() 
                        else file.source().use {  source ->
                            source.buffer().use { bufferedSource ->
                                jsonSerializer.decodeFromString(bufferedSource.readUtf8())
                            }
                        } ?: TabList()

                loadTabsFromDir()
                        ?.forEach { it?.let { fromJson.add(it) } }
                if (fromJson.size() <= fromJson.index) {
                    fromJson.index = fromJson.size() - 1
                }
                return fromJson
            } catch (e: IOException) {
                Timber.e(e)
            }

            return TabList()
        }

        private fun loadTabsFromDir(): List<Tab?>? {
            return itemsDir?.list()
                    ?.map {
                        val json: String = File(itemsDir, it).source().use { source ->
                            source.buffer().use { bufferedSource ->
                                bufferedSource.readUtf8()
                            }
                        }

                        when {
                            json.contains("editorTab") -> jsonSerializer.decodeFromString<EditorTab>(json)
                            json.contains("pdfTab") -> jsonSerializer.decodeFromString<PdfTab>(json)
                            json.contains("articleTab") -> jsonSerializer.decodeFromString<ArticleTab>(json)
                            json.contains("articleListTab") -> jsonSerializer.decodeFromString<ArticleListTab>(json)
                            json.contains("calendarTab") -> jsonSerializer.decodeFromString<CalendarTab>(json)
                            else -> jsonSerializer.decodeFromString<WebTab>(json)
                        }
                    }
        }

        private fun initTabsFile(context: Context) {
            val storeDir = File(context.filesDir, TABS_DIR)
            if (!storeDir.exists()) {
                storeDir.mkdirs()
            }
            tabsFile = File(storeDir, "tabs.json")

            itemsDir = File(context.filesDir, TABS_ITEM_DIR)
            if (itemsDir != null && !(itemsDir as File).exists()) {
                itemsDir?.mkdirs()
            }
        }
    }

    internal fun indexOf(tab: Tab): Int = tabs.indexOf(tab)

    override fun toString(): String = tabs.toString()

    fun updateWithIdAndHistory(idAndHistory: Pair<String, History>) {
        val targetId = idAndHistory.first
        tabs.firstOrNull { it is WebTab && it.id() == targetId }
            ?.let {
                val currentIndex = tabs.indexOf(it)
                (it as? WebTab)?.addHistory(idAndHistory.second)
                tabs.set(currentIndex, it)
                save()
            }
    }

    fun thumbnailNames(): Collection<String> = makeCopyTabs().map { it.thumbnailPath() }

    fun archiveIds(): Collection<String> {
        val idGenerator = IdGenerator()
        return makeCopyTabs().map { idGenerator.from(it.getUrl()) ?: "" }
    }

    fun ids(): Collection<String> {
        return makeCopyTabs().map { it.id() }
    }

    private fun makeCopyTabs(): MutableList<Tab> {
        return ArrayList(tabs)
    }

}
