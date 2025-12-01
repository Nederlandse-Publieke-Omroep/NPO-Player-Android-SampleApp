package nl.npo.player.sampleApp.presentation.compose.views

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.cast.framework.CastStateListener
import nl.npo.player.library.NPOCasting
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.components.CastButton
import nl.npo.player.sampleApp.presentation.compose.components.SettingsActionIcon
import nl.npo.player.sampleApp.presentation.compose.navigation.AppNavHost
import nl.npo.player.sampleApp.presentation.compose.navigation.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(activity: AppCompatActivity) {
        val navController = rememberNavController()
        val gradientColors = listOf(
        colorResource(R.color.gradient_top),
        colorResource(R.color.gradient_mid),
        colorResource(R.color.gradient_bottom),
    )

        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(colors = gradientColors),
                    ),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        if (NPOCasting.isCastingEnabled) {
                            CastButton(activity)
                        }
                        SettingsActionIcon()
                    },
                )
            },
            bottomBar = {
                BottomBar(navController)
            },
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

