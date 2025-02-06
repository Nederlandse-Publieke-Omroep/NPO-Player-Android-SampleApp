package nl.npo.player.sampleApp.tv.presentation.selection

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnActionClickedListener
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import nl.npo.player.sampleApp.shared.model.SourceWrapper
import nl.npo.player.sampleApp.tv.R
import nl.npo.player.sampleApp.tv.presentation.playback.PlaybackActivity
import nl.npo.player.sampleApp.tv.presentation.selection.PlayerActivity.Companion.getSourceWrapper

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class VideoDetailsFragment : DetailsSupportFragment() {
    private var mSelectedMovie: SourceWrapper? = null

    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = activity ?: return
        val context = context ?: return

        mDetailsBackground = DetailsSupportFragmentBackgroundController(this)

        mSelectedMovie = activity.intent.getSourceWrapper()
        if (mSelectedMovie != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            adapter = mAdapter
            initializeBackground(mSelectedMovie)
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeBackground(movie: SourceWrapper?) {
        val context = context ?: return
        mDetailsBackground.enableParallax()
        Glide
            .with(context)
            .asBitmap()
            .centerCrop()
            .error(R.drawable.default_background)
            .load(movie?.imageUrl)
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {
                        mDetailsBackground.coverBitmap = bitmap
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        mDetailsBackground.coverBitmap = null
                    }
                },
            )
    }

    private fun setupDetailsOverviewRow() {
        val context = context ?: return
        val mSelectedMovie = mSelectedMovie ?: return
        Log.d(TAG, "setupDetailsOverviewRow: $mSelectedMovie")
        val row = DetailsOverviewRow(mSelectedMovie)
        row.imageDrawable = ContextCompat.getDrawable(context, R.drawable.default_background)
        val width = convertDpToPixel(context, DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(context, DETAIL_THUMB_HEIGHT)
        Glide
            .with(context)
            .load(mSelectedMovie.imageUrl)
            .centerCrop()
            .error(R.drawable.default_background)
            .into<CustomTarget<Drawable>>(
                object : CustomTarget<Drawable>(width, height) {
                    override fun onResourceReady(
                        drawable: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        Log.d(TAG, "details overview card image url ready: " + drawable)
                        row.imageDrawable = drawable
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        row.imageDrawable = placeholder
                    }
                },
            )

        val actionAdapter = ArrayObjectAdapter()

        if (mSelectedMovie.getStreamLink) {
            actionAdapter.add(
                Action(
                    ACTION_WATCH_PLUS,
                    resources.getString(R.string.watch),
                    resources.getString(R.string.watch_as_plus),
                ),
            )
        }
        actionAdapter.add(
            Action(
                ACTION_WATCH_START,
                resources.getString(R.string.watch),
                if (mSelectedMovie.getStreamLink) {
                    resources.getString(R.string.watch_as_start)
                } else {
                    ""
                },
            ),
        )
        row.actionsAdapter = actionAdapter

        mAdapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        val context = context ?: return
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor =
            ContextCompat.getColor(context, R.color.selected_background)

        // Hook up transition element.
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(
            activity,
            PlayerActivity.SHARED_ELEMENT_NAME,
        )
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true

        detailsPresenter.onActionClickedListener =
            OnActionClickedListener { action ->
                mSelectedMovie?.let {
                    val isPlusUser =
                        when (action.id) {
                            ACTION_WATCH_START -> false
                            ACTION_WATCH_PLUS -> true
                            else -> true
                        }
                    val intent =
                        PlayerActivity.getStartIntent(
                            context,
                            it.copy(overrideIsPlusUser = isPlusUser),
                            PlaybackActivity::class.java,
                        )
                    startActivity(intent)
                }
            }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun convertDpToPixel(
        context: Context,
        dp: Int,
    ): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
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
                    (itemViewHolder?.view as ImageCardView).mainImageView?.let {
                        ActivityOptionsCompat
                            .makeSceneTransitionAnimation(
                                activity,
                                it,
                                PlayerActivity.SHARED_ELEMENT_NAME,
                            ).toBundle()
                    }
                startActivity(intent, bundle)
            }
        }
    }

    companion object {
        private const val TAG = "VideoDetailsFragment"

        private const val ACTION_WATCH_START = 1L
        private const val ACTION_WATCH_PLUS = 2L

        private const val DETAIL_THUMB_WIDTH = 274
        private const val DETAIL_THUMB_HEIGHT = 274
    }
}
