// In cs/uwaterloo/timora/ui/viewmodels/InvitationViewModel.kt
package cs.uwaterloo.timora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.uwaterloo.domain.model.InvitationModel
import cs.uwaterloo.timora.data.repository.AuthRepository
import cs.uwaterloo.timora.ui.viewmodels.states.InvitationWithEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvitationViewModel(
    private val model: InvitationModel,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _invitations = MutableStateFlow<List<InvitationWithEvent>>(emptyList())
    val invitations: StateFlow<List<InvitationWithEvent>> = _invitations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // A flow for sending one-time error messages to the UI (e.g., for a Snackbar)
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    init {
        // Fetch invitations when the ViewModel is created
        fetchPendingInvitations()
    }

    fun fetchPendingInvitations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = authRepository.getCurrentUserId()
                    ?: throw IllegalStateException("User not logged in")

                val invitations = model.getInvitationsByUser(currentUserId)
                val invitationsWithEvents = invitations.mapNotNull { invitation ->
                    try {
                        val event = model.getEventById(invitation.eventId)
                        val host = model.getUserById(invitation.inviterId)

                        if (event != null && host != null) {
                            InvitationWithEvent(
                                id = "${invitation.eventId}_${invitation.inviteeId}",
                                eventId = invitation.eventId,
                                inviteeId = invitation.inviteeId,
                                eventName = event.title,
                                hostName = host.name
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                _invitations.value = invitationsWithEvents
            } catch (e: Exception) {
                _error.emit("Failed to load invitations: ${e.message}")
                _invitations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptInvitation(invitationId: String) {
        viewModelScope.launch {
            try {
                val invitation = _invitations.value.find { it.id == invitationId }
                    ?: throw IllegalArgumentException("Invitation not found")

                model.acceptInvitation(invitation.eventId, invitation.inviteeId)

                _invitations.value = _invitations.value.filterNot { it.id == invitationId }
            } catch (e: Exception) {
                _error.emit("Failed to accept invitation: ${e.message}")
            }
        }
    }

    fun declineInvitation(invitationId: String) {
        viewModelScope.launch {
            try {
                val invitation = _invitations.value.find { it.id == invitationId }
                    ?: throw IllegalArgumentException("Invitation not found")

                model.deleteInvitation(invitation.eventId, invitation.inviteeId)

                _invitations.value = _invitations.value.filterNot { it.id == invitationId }
            } catch (e: Exception) {
                _error.emit("Failed to decline invitation: ${e.message}")
            }
        }
    }
}

