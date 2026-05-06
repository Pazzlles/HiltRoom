package com.example.hiltroom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hiltroom.ui.detail.PostDetailRoute
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
        ) {
            PostDetailRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
