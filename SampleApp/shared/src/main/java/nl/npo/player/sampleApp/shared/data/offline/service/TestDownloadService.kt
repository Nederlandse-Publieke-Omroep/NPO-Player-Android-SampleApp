package nl.npo.player.sampleApp.shared.data.offline.service

import android.R.attr.progress
import android.app.Notification
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.C
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import nl.npo.player.library.data.offline.exoplayer.NPODownloadService
import nl.npo.player.library.domain.offline.models.NPODownloadState
import nl.npo.player.sampleApp.shared.R

class TestDownloadService : NPODownloadService()
