package com.example.hiltroom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hiltroom.ui.detail.PostDetailContentState
import com.example.hiltroom.ui.detail.PostDetailEvent
import com.example.hiltroom.ui.detail.PostDetailRoute
import com.example.hiltroom.ui.detail.PostDetailScreen
import com.example.hiltroom.ui.detail.PostDetailUiState
import com.example.hiltroom.ui.list.PostListRoute

private object Routes {
    const val posts = "posts"
    const val postDetail = "post/{postId}"

    fun postDetail(postId: String): String = "post/$postId"
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.posts,
        modifier = modifier,
    ) {
        composable(Routes.posts) {
            PostListRoute(
                onOpenPost = { postId ->
                    navController.navigate(Routes.postDetail(postId))
                },
            )
        }

        composable(
            route = Routes.postDetail,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                },
            ),
            ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")

            if (postId.isNullOrBlank()) {
                PostDetailScreen(
                    state = PostDetailUiState(
                        postId = "",
                        content = PostDetailContentState.Error(
                            message = "Не удалось открыть экран: отсутствует id поста в маршруте.",
                        ),
                    ),
                    onEvent = { event ->
                        if (event is PostDetailEvent.BackClicked) {
                            navController.popBackStack()
                        }
                    },
                )
            } else {
                PostDetailRoute(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
