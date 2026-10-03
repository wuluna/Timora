package cs.uwaterloo.timora.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cs.uwaterloo.timora.ui.components.AnnouncementsSection
import cs.uwaterloo.timora.ui.components.AttendeesSection
import cs.uwaterloo.timora.ui.components.EventHeader
import cs.uwaterloo.timora.ui.components.ImmutableCategoriesSection
import cs.uwaterloo.timora.ui.components.MutableCategoriesSection
import cs.uwaterloo.timora.ui.utils.isValidDate
import cs.uwaterloo.timora.ui.utils.isValidTime
import cs.uwaterloo.timora.ui.viewmodels.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    viewModel: EventViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    val event by viewModel.event.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val scrollState = rememberScrollState()

    val existingDateTime = event.dateTime.split("T")
    val initialDate = if (existingDateTime.isNotEmpty()) existingDateTime[0] else ""
    val initialTime =
        if (existingDateTime.size > 1) existingDateTime[1].take(5) else "" // Take HH:MM only

    var editedDate by remember(event.id) { mutableStateOf(initialDate) }
    var editedTime by remember(event.id) { mutableStateOf(initialTime) }
    var editedLocation by remember(event.id) { mutableStateOf(event.location) }
    var editedDescription by remember(event.id) { mutableStateOf(event.description) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showMutableCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var showImmutableCategoryDialog by remember { mutableStateOf(false) }
    var newWishlistName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Event") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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


            val vmError by viewModel.error.collectAsState()
            if (vmError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Error: $vmError",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            // Clear the error when clicked
                            viewModel.clearError()
                        }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            if (showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = "Event Details",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editedDate,
                        onValueChange = {
                            editedDate = it
                            showError = false
                        },
                        label = { Text("Date") },
                        placeholder = { Text("2024-12-25") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Format: YYYY-MM-DD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = editedTime,
                        onValueChange = {
                            editedTime = it
                            showError = false
                        },
                        label = { Text("Time") },
                        placeholder = { Text("14:30") },
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Format: HH:MM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = editedLocation,
                onValueChange = { editedLocation = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = editedDescription,
                onValueChange = { editedDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Announcements
            AnnouncementsSection(announcements = event.announcementList)

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Attendees
            AttendeesSection(attendees = event.attendeeList.filter { it.role != cs.uwaterloo.domain.entity.Role.INVITEE })

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Mutable Categories
            if (event.mutableCategoryList.isNotEmpty()) {
                MutableCategoriesSection(
                    categories = event.mutableCategoryList.toList(),
                    isInteractive = false
                )
            }

            OutlinedButton(
                onClick = { showMutableCategoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add Category")
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Immutable Categories
            if (event.immutableCategoryList.isNotEmpty()) {
                ImmutableCategoriesSection(
                    categories = event.immutableCategoryList.toList(),
                    isInteractive = true,
                    currentUserEmail = currentUserEmail ?: "",
                    canAddItems = true,
                    onToggleItem = { categoryName, itemName, userEmail ->
                        viewModel.toggleChecklistItem(categoryName, itemName, userEmail)
                    },
                    onAddItem = { categoryName, itemName, _ ->
                        currentUserId?.let { userId ->
                            viewModel.addItemToImmutableCategory(categoryName, itemName, userId)
                        }
                    }
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }

            OutlinedButton(
                onClick = { showImmutableCategoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add Wishlist")
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (!isValidDate(editedDate)) {
                        showError = true
                        errorMessage = "Please enter a valid date (YYYY-MM-DD)\nExample: 2024-12-25"
                        return@Button
                    }

                    if (!isValidTime(editedTime)) {
                        showError = true
                        errorMessage = "Please enter a valid time (HH:MM)\nExample: 14:30"
                        return@Button
                    }

                    if (editedLocation.isBlank()) {
                        showError = true
                        errorMessage = "Please enter a location"
                        return@Button
                    }

                    val combinedDateTime = "${editedDate}T${editedTime}:00"

                    viewModel.updateEventDateTime(combinedDateTime)
                    viewModel.updateEventLocation(editedLocation)
                    viewModel.updateEventDescription(editedDescription)
                    onSubmit()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Submit Changes",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Add Mutable Category Dialog
    if (showMutableCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showMutableCategoryDialog = false
                newCategoryName = ""
            },
            title = { Text("Add New Category") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter the name for the new category:")
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addMutableCategory(newCategoryName)
                            showMutableCategoryDialog = false
                            newCategoryName = ""
                        }
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMutableCategoryDialog = false
                        newCategoryName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImmutableCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showImmutableCategoryDialog = false
                newWishlistName = ""
            },
            title = { Text("Add New Wishlist") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter the name for the new wishlist:")
                    OutlinedTextField(
                        value = newWishlistName,
                        onValueChange = { newWishlistName = it },
                        label = { Text("Wishlist Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newWishlistName.isNotBlank()) {
                            viewModel.addImmutableCategory(newWishlistName)
                            showImmutableCategoryDialog = false
                            newWishlistName = ""
                        }
                    },
                    enabled = newWishlistName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImmutableCategoryDialog = false
                        newWishlistName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
