package hu.wasteuber.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.wasteuber.app.ui.screens.admin.AdminApplicationsScreen
import hu.wasteuber.app.ui.screens.admin.AdminHomeScreen
import hu.wasteuber.app.ui.screens.admin.AdminLoginScreen
import hu.wasteuber.app.ui.screens.admin.AdminPendingPostsScreen
import hu.wasteuber.app.ui.screens.admin.AdminPostsScreen
import hu.wasteuber.app.ui.screens.admin.AdminUsersScreen
import hu.wasteuber.app.ui.screens.contractor.AvailableJobsScreen
import hu.wasteuber.app.ui.screens.contractor.ContractorApplicationScreen
import hu.wasteuber.app.ui.screens.contractor.ContractorHomeScreen
import hu.wasteuber.app.ui.screens.contractor.JobDetailsScreen
import hu.wasteuber.app.ui.screens.customer.CreateWasteRequestScreen
import hu.wasteuber.app.ui.screens.customer.CustomerRequestsScreen
import hu.wasteuber.app.ui.screens.feed.CreatePostScreen
import hu.wasteuber.app.ui.screens.feed.EditPostScreen
import hu.wasteuber.app.ui.screens.feed.HomeFeedScreen
import hu.wasteuber.app.ui.screens.feed.MyPostsScreen
import hu.wasteuber.app.ui.screens.feed.PostDetailsScreen
import hu.wasteuber.app.ui.screens.feed.PostEditHistoryScreen
import hu.wasteuber.app.ui.screens.login.LoginScreen
import hu.wasteuber.app.ui.screens.notifications.NotificationsScreen
import hu.wasteuber.app.ui.screens.profile.ProfileScreen
import hu.wasteuber.app.ui.screens.profile.PublicUserProfileScreen
import hu.wasteuber.app.ui.screens.register.RegisterScreen
import hu.wasteuber.app.ui.screens.subscription.SubscriptionScreen
import hu.wasteuber.app.viewmodel.AdminModerationViewModel
import hu.wasteuber.app.viewmodel.AuthViewModel
import hu.wasteuber.app.viewmodel.ContractorViewModel
import hu.wasteuber.app.viewmodel.PostMediaViewModel
import hu.wasteuber.app.viewmodel.PostViewModel
import hu.wasteuber.app.viewmodel.SubscriptionViewModel
import hu.wasteuber.app.viewmodel.UserProfileViewModel

@Composable
fun AppNavigation() {

    val navController =
        rememberNavController()

    val authViewModel: AuthViewModel =
        viewModel()

    val contractorViewModel: ContractorViewModel =
        viewModel()

    val postViewModel: PostViewModel =
        viewModel()

    val postMediaViewModel: PostMediaViewModel =
        viewModel()

    val adminModerationViewModel: AdminModerationViewModel =
        viewModel()

    val userProfileViewModel: UserProfileViewModel =
        viewModel()

    val subscriptionViewModel: SubscriptionViewModel =
        viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // ---------------------------------------------------------------------
        // LOGIN
        // ---------------------------------------------------------------------

        composable("login") {

            LoginScreen(
                authViewModel = authViewModel,

                onLoginSuccess = {

                    navController.navigate(
                        "customer_home"
                    ) {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },

                onRegisterClick = {

                    navController.navigate(
                        "register"
                    )
                },

                onAdminLoginClick = {

                    navController.navigate(
                        "admin_login"
                    )
                }
            )
        }

        // ---------------------------------------------------------------------
        // REGISTER
        // ---------------------------------------------------------------------

        composable("register") {

            RegisterScreen(
                authViewModel = authViewModel,

                onRegisterSuccess = {

                    navController.navigate(
                        "customer_home"
                    ) {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // ---------------------------------------------------------------------
        // ADMIN LOGIN
        // ---------------------------------------------------------------------

        composable("admin_login") {

            AdminLoginScreen(
                authViewModel = authViewModel,

                onAdminLoginSuccess = {

                    navController.navigate(
                        "admin_home"
                    ) {

                        popUpTo(
                            "admin_login"
                        ) {
                            inclusive = true
                        }
                    }
                },

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // MAIN FEED
        // ---------------------------------------------------------------------

        composable("customer_home") {

            HomeFeedScreen(
                postViewModel =
                    postViewModel,

                authViewModel =
                    authViewModel,

                onProfileClick = {

                    navController.navigate(
                        "profile"
                    )
                },

                onMyPostsClick = {

                    navController.navigate(
                        "my_posts"
                    )
                },

                onCreateWasteRequestClick = {

                    navController.navigate(
                        "create_request"
                    )
                },

                onMyRequestsClick = {

                    navController.navigate(
                        "customer_requests"
                    )
                },

                onAvailableJobsClick = {

                    navController.navigate(
                        "available_jobs"
                    )
                },

                onNotificationsClick = {

                    navController.navigate(
                        "notifications"
                    )
                },

                onLogoutClick = {

                    authViewModel.logout()

                    navController.navigate(
                        "login"
                    ) {

                        popUpTo(0)
                    }
                },

                onCreatePostClick = {

                    navController.navigate(
                        "create_post"
                    )
                },

                onPostClick = { postId ->

                    navController.navigate(
                        "post_details/$postId"
                    )
                },

                onUserProfileClick = { userId ->

                    navController.navigate(
                        "user_profile/$userId"
                    )
                }
            )
        }

        // ---------------------------------------------------------------------
        // CREATE POST
        // ---------------------------------------------------------------------

        composable(
            "create_post"
        ) {

            CreatePostScreen(
                postViewModel =
                    postViewModel,

                postMediaViewModel =
                    postMediaViewModel,

                authViewModel =
                    authViewModel,

                onPostCreated = {

                    navController.popBackStack()
                },

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // POST DETAILS
        // ---------------------------------------------------------------------

        composable(
            "post_details/{postId}"
        ) { backStackEntry ->

            val postId =
                backStackEntry.arguments
                    ?.getString(
                        "postId"
                    )
                    ?: ""

            PostDetailsScreen(
                postId =
                    postId,

                postViewModel =
                    postViewModel,

                onBackClick = {

                    navController.popBackStack()
                },

                onEditHistoryClick = {
                        selectedPostId ->

                    navController.navigate(
                        "post_edit_history/$selectedPostId"
                    )
                },

                onUserProfileClick = {
                        userId ->

                    navController.navigate(
                        "user_profile/$userId"
                    )
                }
            )
        }

        // ---------------------------------------------------------------------
        // POST EDIT HISTORY
        // ---------------------------------------------------------------------

        composable(
            "post_edit_history/{postId}"
        ) { backStackEntry ->

            val postId =
                backStackEntry.arguments
                    ?.getString(
                        "postId"
                    )
                    ?: ""

            PostEditHistoryScreen(
                postId =
                    postId,

                postViewModel =
                    postViewModel,

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // MY POSTS
        // ---------------------------------------------------------------------

        composable(
            "my_posts"
        ) {

            MyPostsScreen(
                postViewModel =
                    postViewModel,

                onBackClick = {

                    navController.popBackStack()
                },

                onPostClick = {
                        postId ->

                    navController.navigate(
                        "post_details/$postId"
                    )
                },

                onEditPostClick = {
                        postId ->

                    navController.navigate(
                        "edit_post/$postId"
                    )
                }
            )
        }

        // ---------------------------------------------------------------------
        // EDIT POST
        // ---------------------------------------------------------------------

        composable(
            "edit_post/{postId}"
        ) { backStackEntry ->

            val postId =
                backStackEntry.arguments
                    ?.getString(
                        "postId"
                    )
                    ?: ""

            EditPostScreen(
                postId =
                    postId,

                postViewModel =
                    postViewModel,

                postMediaViewModel =
                    postMediaViewModel,

                onSaveSuccess = {

                    navController.popBackStack()
                },

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // CREATE WASTE REQUEST
        // ---------------------------------------------------------------------

        composable(
            "create_request"
        ) {

            CreateWasteRequestScreen(

                onCreateSuccess = {

                    navController.popBackStack()
                },

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // CUSTOMER REQUESTS
        // ---------------------------------------------------------------------

        composable(
            "customer_requests"
        ) {

            CustomerRequestsScreen(

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // CONTRACTOR HOME
        // ---------------------------------------------------------------------

        composable(
            "contractor_home"
        ) {

            ContractorHomeScreen(

                onAvailableJobsClick = {

                    navController.navigate(
                        "available_jobs"
                    )
                },

                onProfileClick = {

                    navController.navigate(
                        "profile"
                    )
                }
            )
        }

        // ---------------------------------------------------------------------
        // AVAILABLE JOBS
        // ---------------------------------------------------------------------

        composable(
            "available_jobs"
        ) {

            AvailableJobsScreen(

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // JOB DETAILS
        // ---------------------------------------------------------------------

        composable(
            "job_details"
        ) {

            JobDetailsScreen()
        }

        // ---------------------------------------------------------------------
        // NOTIFICATIONS
        // ---------------------------------------------------------------------

        composable(
            "notifications"
        ) {

            NotificationsScreen(

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // PROFILE
        // ---------------------------------------------------------------------

        composable(
            "profile"
        ) {

            ProfileScreen(
                authViewModel =
                    authViewModel,

                contractorViewModel =
                    contractorViewModel,

                onContractorApplicationClick = {

                    navController.navigate(
                        "contractor_application"
                    )
                },

                onLogoutClick = {

                    authViewModel.logout()

                    navController.navigate(
                        "login"
                    ) {

                        popUpTo(0)
                    }
                },

                onSubscriptionClick = {

                    navController.navigate(
                        "subscription"
                    )
                }
            )
        }

        // ---------------------------------------------------------------------
        // SUBSCRIPTION
        // ---------------------------------------------------------------------

        composable(
            "subscription"
        ) {

            SubscriptionScreen(
                subscriptionViewModel =
                    subscriptionViewModel,

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // PUBLIC USER PROFILE
        // ---------------------------------------------------------------------

        composable(
            "user_profile/{userId}"
        ) { backStackEntry ->

            val userId =
                backStackEntry.arguments
                    ?.getString(
                        "userId"
                    )
                    ?: ""

            PublicUserProfileScreen(
                userId =
                    userId,

                userProfileViewModel =
                    userProfileViewModel,

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // CONTRACTOR APPLICATION
        // ---------------------------------------------------------------------

        composable(
            "contractor_application"
        ) {

            ContractorApplicationScreen(
                contractorViewModel =
                    contractorViewModel,

                onSubmitSuccess = {

                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------------------
        // ADMIN
        // ---------------------------------------------------------------------

        composable(
            "admin_home"
        ) {

            AdminHomeScreen(

                onFeedClick = {

                    navController.navigate(
                        "customer_home"
                    )
                },

                onPendingPostsClick = {

                    navController.navigate(
                        "admin_pending_posts"
                    )
                },

                onApplicationsClick = {

                    navController.navigate(
                        "admin_applications"
                    )
                },

                onUsersModerationClick = {

                    navController.navigate(
                        "admin_users"
                    )
                },

                onPostsModerationClick = {

                    navController.navigate(
                        "admin_posts"
                    )
                },

                onLogoutClick = {

                    authViewModel.logout()

                    navController.navigate(
                        "login"
                    ) {

                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            "admin_pending_posts"
        ) {

            AdminPendingPostsScreen(
                postViewModel =
                    postViewModel,

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        composable(
            "admin_applications"
        ) {

            AdminApplicationsScreen(
                contractorViewModel =
                    contractorViewModel
            )
        }

        composable(
            "admin_users"
        ) {

            AdminUsersScreen(
                adminModerationViewModel =
                    adminModerationViewModel,

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }

        composable(
            "admin_posts"
        ) {

            AdminPostsScreen(
                postViewModel =
                    postViewModel,

                onBackClick = {

                    navController.popBackStack()
                }
            )
        }
    }
}