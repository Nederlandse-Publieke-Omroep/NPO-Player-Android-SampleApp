package nl.npo.player.sampleApp.tv.presentation.selection

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.player.sampleApp.shared.domain.model.Environment
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsItem
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsPickerOption
import nl.npo.player.sampleApp.shared.presentation.settings.model.SettingsSwitchOption
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LinksViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.MainViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.SettingsViewModel
import nl.npo.player.sampleApp.tv.R
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

/**
 * Loads a grid of cards with movies to browse.
 */
@AndroidEntryPoint
class MainFragment : BrowseSupportFragment() {
    private val mHandler = Handler(Looper.myLooper()!!)
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null
    private val linksViewModel: LinksViewModel by viewModels<LinksViewModel>()
    private val settingsViewModel: SettingsViewModel by viewModels<SettingsViewModel>()
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()
    private var lastKnownEnvironment: Environment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        prepareBackgroundManager()

        setupUIElements()

        setupObservers()

        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun setupObservers() {
        val activity = activity ?: return
        settingsViewModel.initSettingsList(false)

        linksViewModel.streamLinkList.observeNonNull(this) {
            if (isDataReady()) loadRows()
        }
        linksViewModel.urlLinkList.observeNonNull(this) {
            if (isDataReady()) loadRows()
        }
        settingsViewModel.settingsList.observeNonNull(this) {
            if (isDataReady()) loadRows()
        }
        mainViewModel.environment.observeNonNull(this) {
            Log.d(TAG, "Environment update: $it, Last known environment: $lastKnownEnvironment")
            if (lastKnownEnvironment != null && lastKnownEnvironment != it) {
                val i: Intent =
                    activity.packageManager.getLeanbackLaunchIntentForPackage(activity.packageName)
                        ?: run {
                            Log.d(
                                TAG,
                                "No leanback launchIntent for package ${activity.packageName}",
                            )
                            return@observeNonNull
                        }
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(i)
                exitProcess(0)
            }
            lastKnownEnvironment = it
        }
    }

    private fun isDataReady() =
        (
            !linksViewModel.streamLinkList.value.isNullOrEmpty() &&
                !linksViewModel.urlLinkList.value.isNullOrEmpty() &&
                !settingsViewModel.settingsList.value.isNullOrEmpty()
        )

    private fun prepareBackgroundManager() {
        activity?.let { activity ->
            mBackgroundManager = BackgroundManager.getInstance(activity)
            mBackgroundManager.attach(activity.window)
            mDefaultBackground = ContextCompat.getDrawable(activity, R.drawable.default_background)
            mMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(mMetrics)
        }
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        context?.let { context ->
            // set fastLane (or headers) background color
            brandColor = ContextCompat.getColor(context, R.color.fastlane_background)
            // set search icon color
            searchAffordanceColor = ContextCompat.getColor(context, R.color.search_opaque)
        }
    }

    private fun loadRows() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        val headerId = setupStreamLinkRow(cardPresenter, rowsAdapter)
        rowsAdapter.add(setupUrlLinkRow(cardPresenter, headerId + 1))
        rowsAdapter.add(setupSettingsGrid(headerId + 2))

        adapter = rowsAdapter
    }

    private fun setupStreamLinkRow(
        cardPresenter: CardPresenter,
        rowsAdapter: ArrayObjectAdapter,
    ): Long {
        val list = linksViewModel.streamLinkList.value
        var listRowAdapter = ArrayObjectAdapter(cardPresenter)
        var headerId = 0L
        list?.forEachIndexed { index, item ->
            listRowAdapter.add(item)
            if ((index + 1) % 6 == 0) {
                val header =
                    if (headerId == 0L) {
                        HeaderItem(headerId, getString(R.string.browse_category_stream_link))
                    } else {
                        HeaderItem(headerId, "")
                    }
                rowsAdapter.add(ListRow(header, listRowAdapter))
                listRowAdapter = ArrayObjectAdapter(cardPresenter)
                headerId++
            }
        }
        if (listRowAdapter.size() > 0) {
            rowsAdapter.add(ListRow(HeaderItem(headerId, ""), listRowAdapter))
        }
        return headerId
    }

    private fun setupUrlLinkRow(
        cardPresenter: CardPresenter,
        headerId: Long,
    ): ListRow {
        val header = HeaderItem(headerId, getString(R.string.browse_category_url))
        val list = linksViewModel.urlLinkList.value
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        list?.forEach { listRowAdapter.add(it) }
        return ListRow(header, listRowAdapter)
    }

    private fun setupSettingsGrid(headerId: Long): ListRow {
        val gridHeader = HeaderItem(headerId, getString(R.string.browse_category_settings))

        val mGridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        val list = settingsViewModel.settingsList.value
        list?.forEach {
            if (it.isUsefulOnTV()) gridRowAdapter.add(it)
        }
        return ListRow(gridHeader, gridRowAdapter)
    }

    private fun SettingsItem.isUsefulOnTV(): Boolean =
        when (this.titleRes) {
            nl.npo.player.sampleApp.shared.R.string.setting_user_type,
            nl.npo.player.sampleApp.shared.R.string.setting_pause_when_noisy,
            nl.npo.player.sampleApp.shared.R.string.setting_pause_on_cellular,
            nl.npo.player.sampleApp.shared.R.string.setting_enable_casting,
            nl.npo.player.sampleApp.shared.R.string.setting_only_streamlink_random_enabled,
            -> false

            else -> true
        }

    private fun setupEventListeners() {
        if (context == null) return
        setOnSearchClickedListener {
            Toast
                .makeText(context, "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row,
        ) {
            val context = context ?: return
            val activity = activity ?: return
            if (item is SourceWrapper) {
                Log.d(TAG, "onItemClicked: $item")
                val intent =
                    PlayerActivity.getStartIntent(context, item, PlayerActivity::class.java)

                val bundle =
                    (itemViewHolder.view as ImageCardView).mainImageView?.let { imageView ->
                        ActivityOptionsCompat
                            .makeSceneTransitionAnimation(
                                activity,
                                imageView,
                                PlayerActivity.SHARED_ELEMENT_NAME,
                            ).toBundle()
                    }
                startActivity(intent, bundle)
            } else if (item is SettingsItem) {
                when (item) {
                    is SettingsItem.Switch -> {
                        settingsViewModel.handleSettingChange(
                            item.key,
                            SettingsSwitchOption(!item.value.value),
                        )
                    }

                    is SettingsItem.Picker -> {
                        val popupMenu =
                            createPopupMenu(item.options, itemViewHolder.view) { option ->
                                settingsViewModel.handleSettingChange(item.key, option)
                            }
                        popupMenu.show()
                    }
                }
            }
        }
    }

    private fun createPopupMenu(
        options: List<SettingsPickerOption>,
        itemView: View,
        onItemClick: (SettingsPickerOption) -> Unit,
    ): PopupMenu =
        PopupMenu(context, itemView, Gravity.END).apply {
            options.forEachIndexed { index, settingsOption ->
                menu.add(
                    0,
                    index,
                    index,
                    settingsOption.name,
                )
            }

            setOnMenuItemClickListener { item ->
                onItemClick(options[item.itemId])
                true
            }
        }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row,
        ) {
            if (item is SourceWrapper) {
                mBackgroundUri = item.imageUrl
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val context = context ?: return
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide
            .with(context)
            .load(uri)
            .centerCrop()
            .error(mDefaultBackground)
            .into<CustomTarget<Drawable>>(
                object : CustomTarget<Drawable>(width, height) {
                    override fun onResourceReady(
                        drawable: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        mBackgroundManager.drawable = drawable
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        mBackgroundManager.drawable = placeholder
                    }
                },
            )
        mBackgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {
        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(
                ContextCompat.getColor(
                    parent.context,
                    R.color.default_background,
                ),
            )
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return ViewHolder(view)
        }

        override fun onBindViewHolder(
            viewHolder: ViewHolder,
            item: Any?,
        ) {
            val settingsItem = item as? SettingsItem ?: return
            (viewHolder.view as TextView).text =
                when (settingsItem) {
                    is SettingsItem.Switch -> "${getString(settingsItem.titleRes)}:\n${settingsItem.value.value}"
                    is SettingsItem.Picker -> "${getString(settingsItem.titleRes)}:\n${settingsItem.value.name}"
                }
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
    }

    companion object {
        private const val TAG = "MainFragment"

        private const val BACKGROUND_UPDATE_DELAY = 300
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
    }
}
