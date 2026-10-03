package cs.uwaterloo.timora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cs.uwaterloo.data.db.SupabaseDb
import cs.uwaterloo.data.impl.AnnouncementDao
import cs.uwaterloo.data.impl.AttendeeDao
import cs.uwaterloo.data.impl.EventDao
import cs.uwaterloo.data.impl.ImmutableCategoryDao
import cs.uwaterloo.data.impl.InvitationDao
import cs.uwaterloo.data.impl.MutableCategoryDao
import cs.uwaterloo.data.impl.UserDao
import cs.uwaterloo.domain.model.EventModel
import cs.uwaterloo.domain.model.InvitationModel
import cs.uwaterloo.domain.model.UserModel
import cs.uwaterloo.timora.data.repository.AuthRepository
import cs.uwaterloo.timora.ui.screens.DashboardScreen
import cs.uwaterloo.timora.ui.screens.EventEditScreen
import cs.uwaterloo.timora.ui.screens.EventViewEditScreen
import cs.uwaterloo.timora.ui.screens.LaunchPageScreen
import cs.uwaterloo.timora.ui.screens.LoginScreen
import cs.uwaterloo.timora.ui.screens.UserEditScreen
import cs.uwaterloo.timora.ui.screens.UserScreen
import cs.uwaterloo.timora.ui.screens.ViewInvitationsScreen
import cs.uwaterloo.timora.ui.theme.TimoraTheme
import cs.uwaterloo.timora.ui.viewmodels.AuthState
import cs.uwaterloo.timora.ui.viewmodels.AuthViewModel
import cs.uwaterloo.timora.ui.viewmodels.EventViewModel
import cs.uwaterloo.timora.ui.viewmodels.InvitationViewModel
import cs.uwaterloo.timora.ui.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import ui.screens.CreateEventScreen

// Initializing app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = SupabaseDb()

        val userDao = UserDao(database)
        val attendeeDao = AttendeeDao(database)
        val invitationDao = InvitationDao(database)
        val announcementDao = AnnouncementDao(database)
        val mutableCategoryDao = MutableCategoryDao(database)
        val immutableCategoryDao = ImmutableCategoryDao(database)

        val eventDao = EventDao(
            database,
            userDao,
            attendeeDao,
            announcementDao,
            mutableCategoryDao,
            immutableCategoryDao
        )

        val userModel = UserModel(userDao, eventDao, invitationDao)
        InvitationModel(invitationDao, eventDao, userDao, attendeeDao)
        val eventModel = EventModel(
            eventDao,
            attendeeDao,
            announcementDao,
            mutableCategoryDao,
            immutableCategoryDao,
            userDao
        )
        val invitationModel = InvitationModel(invitationDao, eventDao, userDao, attendeeDao)

        val authRepository = AuthRepository(this, userModel)
        val authViewModel = AuthViewModel(authRepository)
        val eventViewModel = EventViewModel(
            eventModel = eventModel,
            authRepository = authRepository,
            userDao = userDao,
            invitationDao = invitationDao,
            attendeeDao = attendeeDao,
            eventDao = eventDao
        )
        val userViewModel = UserViewModel(userModel, authRepository)
        val invitationViewModel = InvitationViewModel(
            model = invitationModel,
            authRepository = authRepository
        )

        setContent {
            TimoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showLaunchScreen by remember { mutableStateOf(true) }
                    var screen by remember { mutableStateOf("loading") }
                    val authState by authViewModel.authState.collectAsState()

                    // Handle auth state changes
                    LaunchedEffect(authState, showLaunchScreen) {
                        if (!showLaunchScreen) {
                            when (authState) {
                                is AuthState.Authenticated -> {
                                    if (screen == "loading") {
                                        screen = "dashboard"
                                    }
                                }

                                is AuthState.Unauthenticated -> screen = "login"
                                is AuthState.Error -> screen = "login"
                                is AuthState.Loading -> {} // Wait
                            }
                        }
                    }

                    if (showLaunchScreen) {
                        LaunchPageScreen()

                        LaunchedEffect(Unit) {
                            delay(2000)
                            showLaunchScreen = false
                        }
                    } else {
                        val userViewModelUser by userViewModel.user.collectAsState()

                        val authenticatedUser =
                            (authState as? AuthState.Authenticated)?.user

                        val currentUser = authenticatedUser ?: userViewModelUser
                        when (screen) {
                            "login" -> LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToDashboard = { screen = "dashboard" }
                            )

                            "dashboard" -> {
                                LaunchedEffect(currentUser) {
                                    currentUser?.let { user ->
                                        eventViewModel.loadUserEvents(user.id)
                                    }
                                }

                                DashboardScreen(
                                    user = currentUser,
                                    userViewModel = userViewModel,
                                    eventViewModel = eventViewModel,
                                    onEventClick = { eventId ->
                                        eventViewModel.loadEvent(eventId)
                                        screen = "event"
                                    },
                                    onCreateEventClick = {
                                        screen = "createEvent"
                                    },
                                    onProfileClick = {
                                        screen = "user"
                                    },
                                    onNotificationClick = {
                                        screen = "invitations"
                                    }
                                )
                            }

                            "createEvent" -> CreateEventScreen(
                                currentUser = currentUser,
                                eventViewModel = eventViewModel,
                                onBackClick = { screen = "dashboard" },
                                onCreateClick = {
                                    currentUser?.let { user ->
                                        eventViewModel.loadUserEvents(user.id)
                                    }
                                    screen = "dashboard"
                                },
                            )

                            "user" -> UserScreen(
                                viewModel = userViewModel,
                                authViewModel = authViewModel,
                                onEditClick = {
                                    userViewModel.startEditing()
                                    screen = "userEdit"
                                },
                                onInvitationsClick = {
                                    screen = "invitations"
                                },
                                onBackClick = { screen = "dashboard" }
                            )

                            "userEdit" -> UserEditScreen(
                                viewModel = userViewModel,
                                onCancelClick = {
                                    userViewModel.cancelEditing()
                                    screen = "user"
                                },
                                onSaveClick = {
                                    userViewModel.submitChanges { screen = "user" }
                                }
                            )

                            "invitations" -> ViewInvitationsScreen(
                                viewModel = invitationViewModel,
                                onBackClick = { screen = "dashboard" }
                            )

                            "event" -> {
                                val isEditScreenVisible by eventViewModel.isEditScreenVisible.collectAsState()

                                if (isEditScreenVisible) {
                                    EventEditScreen(
                                        viewModel = eventViewModel,
                                        onBackClick = { eventViewModel.hideEditScreen() },
                                        onSubmit = { eventViewModel.submitChanges() }
                                    )
                                } else {
                                    EventViewEditScreen(
                                        viewModel = eventViewModel,
                                        onEditClick = { eventViewModel.showEditScreen() },
                                        onBackClick = { screen = "dashboard" }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
