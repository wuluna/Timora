package cs.uwaterloo.timora.ui.viewmodels.states

// Data class to hold combined information for the UI
data class InvitationWithEvent(
    val id: String, // "${eventId}_${inviteeId}"
    val eventId: String,
    val inviteeId: String,
    val eventName: String,
    val hostName: String
)