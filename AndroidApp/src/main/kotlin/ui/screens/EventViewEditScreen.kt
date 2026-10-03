package cs.uwaterloo.timora.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cs.uwaterloo.domain.entity.Role
import cs.uwaterloo.timora.ui.components.AnnouncementsSection
import cs.uwaterloo.timora.ui.components.AttendeesSection
import cs.uwaterloo.timora.ui.components.EventDetailsSection
import cs.uwaterloo.timora.ui.components.EventHeader
import cs.uwaterloo.timora.ui.components.ImmutableCategoriesSection
import cs.uwaterloo.timora.ui.components.InviteAttendeesDialog
import cs.uwaterloo.timora.ui.components.MutableCategoriesSection
import cs.uwaterloo.timora.ui.viewmodels.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventViewEditScreen(
    viewModel: EventViewModel = viewModel(),
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val event by viewModel.event.collectAsState()
    val scrollState = rememberScrollState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var announcementText by remember { mutableStateOf("") }

    val showInviteDialog by viewModel.showInviteDialog.collectAsState()
    val inviteDialogState by viewModel.inviteDialogState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.inviteSuccess.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val canUserEdit = remember(currentUserId, event) {
        currentUserId?.let { userId ->
            if (event.host.id == userId) {
                true
            } else {
                val userAttendee = event.attendeeList.find { it.userId == userId }
                userAttendee?.role == Role.HOST || userAttendee?.role == Role.CO_HOST
            }
        } ?: false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                },
                actions = {
                    if (canUserEdit) {
                        IconButton(
                            onClick = { showAnnouncementDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Add Announcement"
                            )
                        }
                    }
                    if (canUserEdit) {
                        IconButton(
                            onClick = { viewModel.openInviteDialog() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Invite Attendees"
                            )
                        }
                    }
                    if (canUserEdit) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Event"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Event Header
            EventHeader(event = event)

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Event Details
            EventDetailsSection(event = event, isEditMode = false)

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Announcements
            AnnouncementsSection(announcements = event.announcementList)

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Attendees
            AttendeesSection(attendees = event.attendeeList.filter { it.role != Role.INVITEE })

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Mutable Categories
            if (event.mutableCategoryList.isNotEmpty()) {
                MutableCategoriesSection(
                    categories = event.mutableCategoryList.toList(),
                    isInteractive = true,
                    currentUserEmail = currentUserEmail ?: "",
                    onAddItem = { categoryName, itemName, addedBy ->
                        viewModel.addItemToMutableCategory(categoryName, itemName, addedBy)
                    }
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }

            // Immutable Categories
            if (event.immutableCategoryList.isNotEmpty()) {
                val canAddItems = currentUserId?.let { userId ->
                    event.attendeeList.find { attendee ->
                        attendee.userId == userId
                    }?.let { attendee ->
                        attendee.role == Role.HOST ||
                                attendee.role == Role.CO_HOST
                    }
                } ?: false

                ImmutableCategoriesSection(
                    categories = event.immutableCategoryList.toList(),
                    isInteractive = true,
                    currentUserEmail = currentUserEmail ?: "",
                    canAddItems = canAddItems,
                    onToggleItem = { categoryName, itemName, userEmail ->
                        viewModel.toggleChecklistItem(categoryName, itemName, userEmail)
                    },
                    onAddItem = { categoryName, itemName, _ ->
                        currentUserId?.let { userId ->
                            viewModel.addItemToImmutableCategory(categoryName, itemName, userId)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAnnouncementDialog) {
        AlertDialog(
            onDismissRequest = {
                showAnnouncementDialog = false
                announcementText = ""
            },
            title = { Text("Add Announcement") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter your announcement for all attendees:")
                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = { announcementText = it },
                        label = { Text("Announcement") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (announcementText.isNotBlank() && currentUserId != null) {
                            viewModel.addAnnouncement(
                                userId = currentUserId!!,
                                text = announcementText
                            )
                            showAnnouncementDialog = false
                            announcementText = ""
                        }
                    },
                    enabled = announcementText.isNotBlank()
                ) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAnnouncementDialog = false
                        announcementText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInviteDialog) {
        InviteAttendeesDialog(
            state = inviteDialogState,
            onDismiss = { viewModel.closeInviteDialog() },
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onInviteUser = { userId -> viewModel.sendInvitation(userId) }
        )
    }
}