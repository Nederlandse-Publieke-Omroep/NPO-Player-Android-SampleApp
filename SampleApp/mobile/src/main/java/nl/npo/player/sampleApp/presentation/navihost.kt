package nl.npo.player.sampleApp.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import nl.npo.player.sampleApp.presentation.compose.AudioList
import nl.npo.player.sampleApp.presentation.compose.VideoList
import nl.npo.player.sampleApp.shared.model.SourceWrapper

@Composable
fun AppNavHost(
    navController: NavHostController,
    videos: List<SourceWrapper>,
    audios: List<SourceWrapper>
) {
    NavHost(
         navController = navController,
         startDestination = Routes.VIDEO_LIST
     ) {

        composable(Routes.VIDEO_LIST) {
            VideoList(
                videos,
            ) { }
        }
        
        composable(Routes.AUDIO_LIST) {
            AudioList(audios) { }
        }


         }
     }

