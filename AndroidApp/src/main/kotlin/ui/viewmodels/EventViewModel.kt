package cs.uwaterloo.timora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.uwaterloo.data.dao.IAttendeeDao
import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IInvitationDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.domain.entity.ImmutableCategory
import cs.uwaterloo.domain.entity.MutableCategory
import cs.uwaterloo.domain.entity.Role
import cs.uwaterloo.domain.entity.User
import cs.uwaterloo.domain.model.EventModel
import cs.uwaterloo.domain.model.InvitationModel
import cs.uwaterloo.timora.data.repository.AuthRepository
import cs.uwaterloo.timora.ui.viewmodels.states.InviteAttendeeState
import cs.uwaterloo.timora.ui.viewmodels.states.toInvitableUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Event UI screens.
 *
 * TODO: Wire up EventModel via dependency injection or manual instantiation.
 * For now, using sample data. To connect to real database:
 *
 * 1. Instantiate DAOs with SupabaseDb
 * 2. Pass DAOs to EventModel constructor
 * 3. Pass EventModel to this ViewModel
 * 4. Call eventModel.getEventById() in init block
 */
class EventViewModel(
    private val eventModel: EventModel? = null,  // Optional for now, will be required later
    private val authRepository: AuthRepository? = null,
    private val userDao: IUserDao? = null,
    private val invitationDao: IInvitationDao? = null,
    private val attendeeDao: IAttendeeDao? = null,
    private val eventDao: IEventDao? = null
) : ViewModel() {

    private val _event = MutableStateFlow(createSampleEvent())
    val event: StateFlow<Event> = _event.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    private val _isEditScreenVisible = MutableStateFlow(false)
    val isEditScreenVisible: StateFlow<Boolean> = _isEditScreenVisible.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Invite attendees dialog state
    private val _inviteDialogState = MutableStateFlow(InviteAttendeeState())
    val inviteDialogState: StateFlow<InviteAttendeeState> = _inviteDialogState.asStateFlow()

    private val _showInviteDialog = MutableStateFlow(false)
    val showInviteDialog: StateFlow<Boolean> = _showInviteDialog.asStateFlow()

    private val _inviteSuccess = MutableSharedFlow<String>()
    val inviteSuccess: SharedFlow<String> = _inviteSuccess.asSharedFlow()

    init {
        loadCurrentUserEmail()
    }

    /**
     * Loads an event from the database.
     * Call this method when EventModel is wired up.
     */
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                eventModel?.getEventById(eventId)?.let { loadedEvent ->
                    _event.value = loadedEvent
                } ?: run {
                    _error.value = "Event not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load event: ${e.message}"
            }
        }
    }

    /**
     * Loads all events for a user (both hosted and attending).
     * Call this method after user logs in to populate the dashboard.
     */
    fun loadUserEvents(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Get all events for the user (both hosted and attending)
                val events = eventModel?.getAllUserEvents(userId) ?: emptyList()
                _userEvents.value = events

            } catch (e: Exception) {
                _error.value = "Failed to load events: ${e.message}"
                _userEvents.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createEvent(
        title: String,
        dateTime: String,
        location: String,
        description: String,
        hostUser: User,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                if (title.isBlank() || dateTime.isBlank() || location.isBlank()) {
                    onFailure("You have unfilled fields")
                    return@launch
                }

                val eventId = java.util.UUID.randomUUID().toString()

                val newEvent = Event(
                    id = eventId,
                    title = title,
                    host = hostUser,
                    dateTime = dateTime,
                    location = location,
                    description = description
                )

                eventModel?.createEvent(newEvent)
                loadUserEvents(hostUser.id)
                onSuccess()

            } catch (e: Exception) {
                val errorMsg = "Failed to create event: ${e.message}"
                _error.value = errorMsg
                onFailure(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showEditScreen() {
        _isEditScreenVisible.value = true
    }

    fun hideEditScreen() {
        _isEditScreenVisible.value = false
    }

    /**
     * Saves changes to the event.
     * When EventModel is wired up, this will persist to database.
     */
    fun submitChanges() {
        viewModelScope.launch {
            try {
                if (eventModel != null) {
                    // Persist changes via Model
                    eventModel.updateEvent(_event.value)
                }
                hideEditScreen()
            } catch (e: Exception) {
                _error.value = "Failed to save changes: ${e.message}"
            }
        }
    }

    /**
     * Adds an item to a mutable category.
     * For now, manipulates local state. When EventModel is wired up, this will persist.
     */
    fun addItemToMutableCategory(categoryName: String, itemName: String, addedBy: String) {
        viewModelScope.launch {
            try {
                val currentEvent = _event.value
                val category = currentEvent.mutableCategoryList.find { it.name == categoryName }

                if (category == null) {
                    return@launch
                }

                category?.addItem(addedBy, itemName)

                if (eventModel != null) {
                    eventModel.updateMutableCategoryItems(currentEvent.id, categoryName, category)
                    loadEvent(currentEvent.id)
                } else {
                    _event.update { it.copy() }
                }

            } catch (e: Exception) {
                _error.value = "Failed to add item: ${e.message}"
            }
        }
    }

    /**
     * Toggles a checklist item in an immutable category.
     * For now, manipulates local state. When EventModel is wired up, this will persist.
     */
    fun toggleChecklistItem(categoryName: String, itemName: String, userEmail: String) {
        viewModelScope.launch {
            try {
                val currentEvent = _event.value
                val category = currentEvent.immutableCategoryList.find { it.name == categoryName }
                category?.let {
                    val checkedUsers = it.items[itemName] ?: emptySet()
                    if (checkedUsers.contains(userEmail)) {
                        it.uncheckItem(userEmail, itemName)
                    } else {
                        it.checkItem(userEmail, itemName)
                    }

                    if (eventModel != null) {
                        eventModel.updateImmutableCategoryItems(
                            currentEvent.id,
                            categoryName,
                            category
                        )
                        loadEvent(currentEvent.id)
                    } else {
                        _event.update { it.copy() }
                    }

                }
            } catch (e: Exception) {
                _error.value = "Failed to toggle item: ${e.message}"
            }
        }
    }

    /**
     * Adds a mutable category to the event.
     * When EventModel is wired up, this will persist to database.
     */
    fun addMutableCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                val currentEvent = _event.value
                val newCategory = MutableCategory(categoryName)

                if (eventModel != null) {
                    // Persist via Model (calls domain entity validation)
                    eventModel.addMutableCategoryToEvent(currentEvent.id, newCategory)
                    // Reload event to get updated state
                    loadEvent(currentEvent.id)
                } else {
                    // Local-only for now
                    currentEvent.addMutableCategory(newCategory)
                    _event.value = currentEvent
                }
            } catch (e: Exception) {
                _error.value = "Failed to add category: ${e.message}"
            }
        }
    }

    fun addImmutableCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                val currentEvent = _event.value
                val newCategory = ImmutableCategory(categoryName)

                if (eventModel != null) {
                    eventModel.addImmutableCategoryToEvent(currentEvent.id, newCategory)
                    loadEvent(currentEvent.id)
                } else {
                    currentEvent.addImmutableCategory(newCategory)
                    _event.value = currentEvent
                }
            } catch (e: Exception) {
                _error.value = "Failed to add wishlist: ${e.message}"
            }
        }
    }

    fun addItemToImmutableCategory(categoryName: String, itemName: String, userId: String) {
        viewModelScope.launch {
            try {
                val currentEvent = _event.value
                val category = currentEvent.immutableCategoryList.find { it.name == categoryName }

                if (category == null) {
                    _error.value = "Category '$categoryName' not found"
                    return@launch
                }

                val attendee = currentEvent.attendeeList.find { it.userId == userId }
                if (attendee == null) {
                    _error.value =
                        "You are not in the attendee list. Only event attendees can add items."
                    return@launch
                }

                // only host/co-host can add an item
                category.addItem(attendee, itemName)

                if (eventModel != null) {
                    eventModel.updateImmutableCategoryItems(currentEvent.id, categoryName, category)
                    loadEvent(currentEvent.id)
                } else {
                    _event.update { currentEvent.copy() }
                }

            } catch (e: Exception) {
                _error.value = "Failed to add item: ${e.message}"
            }
        }
    }

    /**
     * Adds an announcement to the event.
     * Only the host can add announcements (validated by domain entity).
     */
    fun addAnnouncement(userId: String, text: String) {
        viewModelScope.launch {
            try {
                val currentEvent = _event.value

                if (eventModel != null) {
                    // Persist via Model (validates user is host!)
                    eventModel.addAnnouncementToEvent(currentEvent.id, userId, text)
                    // Reload event
                    loadEvent(currentEvent.id)
                } else {
                    _error.value = "EventModel not wired up yet"
                }
            } catch (e: Exception) {
                _error.value = "Failed to add announcement: ${e.message}"
            }
        }
    }

    fun updateEventDateTime(newDateTime: String) {
        _event.update { currentEvent ->
            currentEvent.updateDateTime(newDateTime)
            currentEvent.copy()  // Return a new copy
        }
    }

    fun updateEventLocation(newLocation: String) {
        _event.update { currentEvent ->
            currentEvent.updateLocation(newLocation)
            currentEvent.copy()
        }
    }

    fun updateEventDescription(newDescription: String) {
        _event.update { currentEvent ->
            currentEvent.description = newDescription
            currentEvent.updatedAt = java.time.Instant.now().toString()
            currentEvent.copy()
        }
    }

    private fun loadCurrentUserEmail() {
        viewModelScope.launch {
            try {
                _currentUserEmail.value = authRepository?.getCurrentUserEmail()
                _currentUserId.value = authRepository?.getCurrentUserId()
            } catch (e: Exception) {
                _error.value = "Could not load user email: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun openInviteDialog() {
        viewModelScope.launch {
            _showInviteDialog.value = true
            loadInvitableUsers()
        }
    }

    fun closeInviteDialog() {
        _showInviteDialog.value = false
        _inviteDialogState.value = InviteAttendeeState()
    }

    private suspend fun loadInvitableUsers() {
        try {
            _inviteDialogState.update { it.copy(isLoading = true, errorMessage = null) }

            val eventId = _event.value.id
            val invitationModel = InvitationModel(
                invitationDao!!, eventDao!!, userDao!!, attendeeDao!!
            )

            val users = invitationModel.getUsersForInvitation(eventId)
            val invitableUsers = users.map { it.toInvitableUser() }

            _inviteDialogState.update {
                it.copy(
                    availableUsers = invitableUsers,
                    filteredUsers = invitableUsers,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _inviteDialogState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load users: ${e.message}"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _inviteDialogState.update { state ->
            val filtered = if (query.isBlank()) {
                state.availableUsers
            } else {
                state.availableUsers.filter { user ->
                    user.name.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredUsers = filtered)
        }
    }

    fun sendInvitation(userId: String) {
        viewModelScope.launch {
            try {
                val eventId = _event.value.id
                val currentUserId = _currentUserId.value
                    ?: throw IllegalStateException("Current user not found")

                val invitationModel = InvitationModel(
                    invitationDao!!, eventDao!!, userDao!!, attendeeDao!!
                )

                invitationModel.sendInvitationWithValidation(
                    eventId = eventId,
                    inviterId = currentUserId,
                    inviteeId = userId
                )

                // Get user name for success message
                val userName = _inviteDialogState.value.availableUsers
                    .find { it.id == userId }?.name ?: "User"

                // Remove invited user from list
                _inviteDialogState.update { state ->
                    val updated = state.availableUsers.filter { it.id != userId }
                    val filteredUpdated = state.filteredUsers.filter { it.id != userId }
                    state.copy(
                        availableUsers = updated,
                        filteredUsers = filteredUpdated,
                        errorMessage = null
                    )
                }

                _inviteSuccess.emit("Invitation sent to $userName")

            } catch (e: Exception) {
                _inviteDialogState.update {
                    it.copy(errorMessage = e.message ?: "Failed to send invitation")
                }
            }
        }
    }

    private fun createSampleEvent(): Event {
        val host = User(
            id = "user1",
            email = "alice.johnson@gmail.com",
            name = "Alice Johnson",
            avatarUrl = "",
            bio = "Event host and coordinator"
        )

        val event = Event(
            id = "event1",
            title = "CS 346 Project Demo Day",
            host = host,
            dateTime = "2024-12-15T14:00:00",
            location = "E7 Atrium, University of Waterloo",
            description = "Join us for the final project demonstrations for CS 346. Students will showcase their Android applications built throughout the term."
        )

        // Add attendees
        event.addAttendee(
            User("user2", "bob.smith@gmail.com", "Bob Smith", "", ""),
            Role.CO_HOST
        )
        event.addAttendee(
            User("user3", "carol.white@gmail.com", "Carol White", "", ""),
            Role.GUEST
        )
        event.addAttendee(
            User("user4", "david.brown@gmail.com", "David Brown", "", ""),
            Role.INVITEE
        )

        // Add announcements
        event.addAnnouncement(
            host,
            "Welcome everyone! Please arrive 15 minutes early to set up your demo station."
        )
        event.addAnnouncement(
            host,
            "Reminder: Each team has 10 minutes for their presentation."
        )

        // Add mutable categories (potluck style)
        val foodCategory = MutableCategory("Food & Drinks")
        foodCategory.addItem("alice.johnson@gmail.com", "Pizza")
        foodCategory.addItem("bob.smith@gmail.com", "Soft Drinks")
        foodCategory.addItem("carol.white@gmail.com", "Cookies")
        event.addMutableCategory(foodCategory)

        val equipmentCategory = MutableCategory("Equipment")
        equipmentCategory.addItem("alice.johnson@gmail.com", "Laptop and adapter")
        equipmentCategory.addItem("bob.smith@gmail.com", "HDMI cable")
        equipmentCategory.addItem("carol.white@gmail.com", "Extension cord")
        event.addMutableCategory(equipmentCategory)

        // Add immutable category (checklist)
        val setupCategory = ImmutableCategory("Setup Checklist")
        setupCategory.addItem(
            Attendee("user1", "Alice Johnson", Role.HOST),
            "Set up projector"
        )
        setupCategory.addItem(
            Attendee("user1", "Alice Johnson", Role.HOST),
            "Arrange tables"
        )
        setupCategory.addItem(
            Attendee("user1", "Alice Johnson", Role.HOST),
            "Test WiFi connection"
        )
        setupCategory.addItem(
            Attendee("user1", "Alice Johnson", Role.HOST),
            "Prepare voting forms"
        )
        setupCategory.checkItem("alice.johnson@gmail.com", "Set up projector")
        setupCategory.checkItem("bob.smith@gmail.com", "Test WiFi connection")
        event.addImmutableCategory(setupCategory)

        return event
    }

    private fun createSampleEventsList(): List<Event> {
        val sampleUser1 = User(
            id = "user1",
            email = "alice.johnson@gmail.com",
            name = "Alice Johnson",
            avatarUrl = "",
            bio = "Event host and coordinator"
        )

        val sampleUser2 = User(
            id = "user2",
            email = "bob.smith@gmail.com",
            name = "Bob Smith",
            avatarUrl = "",
            bio = "Loves everything related with events"
        )

        // first event where user is owner
        val event1 = Event(
            id = "event1",
            title = "Summer BBQ Party",
            host = sampleUser1,
            dateTime = "11/14/2025 at 18:00",
            location = "123 Garden Street, Park View",
            description = "Annual summer barbecue party",
        ).apply {
            addAttendee(sampleUser1, Role.HOST)
        }

        // second event where user is guest
        val event2 = Event(
            id = "event2",
            title = "Birthday Celebration",
            host = sampleUser2,
            dateTime = "11/19/2025 at 19:00",
            location = "Downtown Community Center",
            description = "Bob's birthday party",
        ).apply {
            addAttendee(sampleUser2, Role.HOST)
            addAttendee(sampleUser1, Role.GUEST)
        }

        return listOf(event1, event2)
    }
}
