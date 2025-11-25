package nl.npo.player.sampleApp.presentation.compose.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import nl.npo.player.library.NPOCasting
import nl.npo.player.sampleApp.R
import nl.npo.player.sampleApp.presentation.compose.components.CastButton
import nl.npo.player.sampleApp.presentation.compose.components.SettingsActionIcon
import nl.npo.player.sampleApp.presentation.compose.navigation.AppNavHost
import nl.npo.player.sampleApp.presentation.compose.navigation.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val nav = rememberNavController()
    Scaffold(
        Modifier.background(
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color.Black.copy(alpha = 0.25f),
                        Color.Transparent,
                    ),
            ),
        ),
        containerColor = Color.Transparent,
        bottomBar = { BottomBar(nav) },
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                    ),
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    if (NPOCasting.isCastingEnabled)
                        {
                            CastButton()
                        }
                    SettingsActionIcon()
                },
            )
        },
    ) { innerPadding ->
        AppNavHost(
            navController = nav,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
