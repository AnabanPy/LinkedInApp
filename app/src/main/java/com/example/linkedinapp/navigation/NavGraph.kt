package com.example.linkedinapp.navigation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.linkedinapp.data.AppDatabase
import com.example.linkedinapp.ui.screens.ChatScreen
import com.example.linkedinapp.ui.screens.CreateJobScreen
import com.example.linkedinapp.ui.screens.EditJobScreen
import com.example.linkedinapp.ui.screens.FeedScreen
import com.example.linkedinapp.ui.screens.JobDetailScreen
import com.example.linkedinapp.ui.screens.LoginScreen
import com.example.linkedinapp.ui.screens.MainScreen
import com.example.linkedinapp.ui.screens.MessagesScreen
import com.example.linkedinapp.ui.screens.MyJobsScreen
import com.example.linkedinapp.ui.screens.ProfileScreen
import com.example.linkedinapp.ui.screens.RegistrationScreen
import com.example.linkedinapp.ui.screens.SearchScreen
import com.example.linkedinapp.ui.screens.SettingsScreen
import com.example.linkedinapp.ui.screens.WelcomeScreen
import com.example.linkedinapp.repository.JobRepository
import com.example.linkedinapp.repository.MessageRepository
import com.example.linkedinapp.repository.UserRepository
import com.example.linkedinapp.util.SessionManager
import com.example.linkedinapp.viewmodel.AuthViewModel
import com.example.linkedinapp.viewmodel.AuthViewModelFactory
import com.example.linkedinapp.viewmodel.JobsViewModelFactory
import com.example.linkedinapp.viewmodel.MessagesViewModelFactory
import com.example.linkedinapp.viewmodel.SearchViewModelFactory

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Registration : Screen("registration")
    object Login : Screen("login")
    object Main : Screen("main")
    object Profile : Screen("profile")
    object MyJobs : Screen("my_jobs")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: Long) = "user_profile/$userId"
    }
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Messages : Screen("messages")
    object Feed : Screen("feed")
    object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: Long) = "job_detail/$jobId"
    }
    object CreateJob : Screen("create_job")
    object EditJob : Screen("edit_job/{jobId}") {
        fun createRoute(jobId: Long) = "edit_job/$jobId"
    }
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: Long) = "chat/$userId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    context: Context = LocalContext.current,
    initialChatUserId: Long? = null
) {
    val database = AppDatabase.getDatabase(context)
    
    // Создаем репозитории
    val userRepository = remember { UserRepository(database.userDao(), context) }
    val messageRepository = remember { MessageRepository(database.messageDao(), context) }
    val jobRepository = remember { JobRepository(database.jobDao(), context) }
    val sessionManager = remember { SessionManager(context) }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userRepository, sessionManager, context)
    )
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isGuest by authViewModel.isGuest.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoadingSession by authViewModel.isLoadingSession.collectAsState()
    
    // Обрабатываем навигацию из уведомления
    LaunchedEffect(initialChatUserId, isLoggedIn, isLoadingSession) {
        if (!isLoadingSession && isLoggedIn && !isGuest && initialChatUserId != null) {
            navController.navigate(Screen.Chat.createRoute(initialChatUserId)) {
                // Очищаем весь стек до Main экрана
                popUpTo(Screen.Main.route) { inclusive = false }
            }
        }
    }
    
    // Показываем индикатор загрузки, пока проверяем сессию
    if (isLoadingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Main.route else Screen.Welcome.route
        ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Registration.route)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onGuestClick = {
                    authViewModel.loginAsGuest()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Registration.route) {
            RegistrationScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Registration.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginError = {
                    // Ошибка уже отображается на экране
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Main.route) {
            val searchViewModelFactory = SearchViewModelFactory(userRepository)
            val currentUserValue = currentUser
            val messagesViewModelFactory = if (!isGuest && currentUserValue != null) {
                MessagesViewModelFactory(
                    messageRepository,
                    userRepository,
                    currentUserValue.id,
                    context
                )
            } else null
            
            MainScreen(
                userName = currentUser?.firstName,
                profilePhotoId = currentUser?.profilePhotoId ?: 0,
                profilePhotoUrl = currentUser?.profilePhotoUrl,
                isGuest = isGuest,
                currentUserId = currentUser?.id,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToMyJobs = {
                    navController.navigate(Screen.MyJobs.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToChat = { userId ->
                    navController.navigate(Screen.Chat.createRoute(userId))
                },
                onNavigateToMessages = {
                    navController.navigate(Screen.Messages.route)
                },
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                searchViewModelFactory = searchViewModelFactory,
                messagesViewModelFactory = messagesViewModelFactory
            )
        }
        
        composable(Screen.Profile.route) {
            val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
            ProfileScreen(
                user = currentUser,
                isGuest = isGuest,
                isOwnProfile = true,
                onBackClick = {
                    navController.popBackStack()
                },
                onPhotoSelected = { photoId ->
                    authViewModel.updateProfilePhoto(photoId)
                },
                onJobClick = { jobId ->
                    navController.navigate(Screen.JobDetail.createRoute(jobId))
                },
                onEditJob = { jobId ->
                    navController.navigate(Screen.EditJob.createRoute(jobId))
                },
                jobsViewModelFactory = jobsViewModelFactory
            )
        }
        
        composable(Screen.MyJobs.route) {
            val currentUserValue = currentUser
            if (!isGuest && currentUserValue != null) {
                val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
                MyJobsScreen(
                    currentUserId = currentUserValue.id,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onJobClick = { jobId ->
                        navController.navigate(Screen.JobDetail.createRoute(jobId))
                    },
                    onEditJob = { jobId ->
                        navController.navigate(Screen.EditJob.createRoute(jobId))
                    },
                    onCreateJob = {
                        navController.navigate(Screen.CreateJob.route)
                    },
                    jobsViewModelFactory = jobsViewModelFactory
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Войдите в систему для просмотра вакансий")
                }
            }
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Search.route) {
            val searchViewModelFactory = SearchViewModelFactory(userRepository)
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = { user ->
                    navController.navigate(Screen.UserProfile.createRoute(user.id))
                },
                searchViewModelFactory = searchViewModelFactory
            )
        }
        
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId")
            if (userId != null) {
                var user by remember { mutableStateOf<com.example.linkedinapp.data.User?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                
                LaunchedEffect(userId) {
                    user = userRepository.getUserById(userId)
                    isLoading = false
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (user != null) {
                    val currentUserValue = currentUser
                    val userValue = user!!
                    val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
                    ProfileScreen(
                        user = userValue,
                        isGuest = false,
                        isOwnProfile = currentUserValue?.id == userValue.id,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onPhotoSelected = if (currentUserValue?.id == userValue.id) {
                            { photoId ->
                                authViewModel.updateProfilePhoto(photoId)
                            }
                        } else { _ -> },
                        onWriteMessage = if (!isGuest && currentUserValue != null && currentUserValue.id != userValue.id) {
                            {
                                navController.navigate(Screen.Chat.createRoute(userValue.id))
                            }
                        } else null,
                        onJobClick = { jobId ->
                            navController.navigate(Screen.JobDetail.createRoute(jobId))
                        },
                        onEditJob = if (currentUserValue?.id == userValue.id) {
                            { jobId ->
                                navController.navigate(Screen.EditJob.createRoute(jobId))
                            }
                        } else null,
                        jobsViewModelFactory = jobsViewModelFactory
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Пользователь не найден")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Назад")
                            }
                        }
                    }
                }
            }
        }
        
        composable(Screen.Messages.route) {
            val currentUserValue = currentUser
            if (!isGuest && currentUserValue != null) {
                val messagesViewModelFactory = MessagesViewModelFactory(
                    messageRepository,
                    userRepository,
                    currentUserValue.id,
                    context
                )
                MessagesScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onConversationClick = { userId ->
                        navController.navigate(Screen.Chat.createRoute(userId))
                    },
                    messagesViewModelFactory = messagesViewModelFactory
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Войдите в систему для просмотра сообщений")
                }
            }
        }
        
        composable(Screen.Feed.route) {
            val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
            FeedScreen(
                userName = currentUser?.firstName,
                profilePhotoId = currentUser?.profilePhotoId ?: 0,
                profilePhotoUrl = currentUser?.profilePhotoUrl,
                isGuest = isGuest,
                currentUserId = currentUser?.id,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToMyJobs = {
                    navController.navigate(Screen.MyJobs.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToMessages = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                onNavigateToJobDetail = { jobId ->
                    navController.navigate(Screen.JobDetail.createRoute(jobId))
                },
                onNavigateToCreateJob = {
                    navController.navigate(Screen.CreateJob.route)
                },
                onWriteClick = if (!isGuest && currentUser != null) {
                    { employerId ->
                        navController.navigate(Screen.Chat.createRoute(employerId))
                    }
                } else null,
                jobsViewModelFactory = jobsViewModelFactory
            )
        }
        
        composable(
            route = Screen.JobDetail.route,
            arguments = listOf(navArgument("jobId") { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong("jobId")
            if (jobId != null) {
                val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
                JobDetailScreen(
                    jobId = jobId,
                    currentUserId = currentUser?.id,
                    isGuest = isGuest,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onWriteClick = { employerId ->
                        navController.navigate(Screen.Chat.createRoute(employerId))
                    },
                    jobsViewModelFactory = jobsViewModelFactory
                )
            }
        }
        
        composable(Screen.CreateJob.route) {
            val currentUserValue = currentUser
            if (!isGuest && currentUserValue != null) {
                val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
                CreateJobScreen(
                    employerId = currentUserValue.id,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onJobCreated = {
                        navController.popBackStack()
                    },
                    jobsViewModelFactory = jobsViewModelFactory
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Войдите в систему для создания вакансии")
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Назад")
                        }
                    }
                }
            }
        }
        
        composable(
            route = Screen.EditJob.route,
            arguments = listOf(navArgument("jobId") { type = NavType.LongType })
        ) { backStackEntry ->
            val currentUserValue = currentUser
            val jobId = backStackEntry.arguments?.getLong("jobId")
            if (!isGuest && currentUserValue != null && jobId != null) {
                val jobsViewModelFactory = JobsViewModelFactory(jobRepository)
                EditJobScreen(
                    jobId = jobId,
                    employerId = currentUserValue.id,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onJobUpdated = {
                        navController.popBackStack()
                    },
                    jobsViewModelFactory = jobsViewModelFactory
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Войдите в систему для редактирования вакансии")
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Назад")
                        }
                    }
                }
            }
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val currentUserValue = currentUser
            if (!isGuest && currentUserValue != null) {
                val userId = backStackEntry.arguments?.getLong("userId")
                if (userId != null) {
                    var otherUser by remember { mutableStateOf<com.example.linkedinapp.data.User?>(null) }
                    var isLoading by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(userId) {
                        otherUser = userRepository.getUserById(userId)
                        isLoading = false
                    }
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (otherUser != null) {
                        val messagesViewModelFactory = MessagesViewModelFactory(
                            messageRepository,
                            userRepository,
                            currentUserValue.id,
                            context
                        )
                        ChatScreen(
                            otherUser = otherUser!!,
                            currentUserId = currentUserValue.id,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToProfile = { userId ->
                                navController.navigate(Screen.UserProfile.createRoute(userId))
                            },
                            messagesViewModelFactory = messagesViewModelFactory
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Пользователь не найден")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Назад")
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Войдите в систему для отправки сообщений")
                }
            }
        }
        }
    }
}

