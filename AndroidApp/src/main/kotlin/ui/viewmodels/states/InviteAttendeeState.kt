package cs.uwaterloo.timora.ui.viewmodels.states

import cs.uwaterloo.domain.entity.User

data class InvitableUser(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String
)

data class InviteAttendeeState(
    val availableUsers: List<InvitableUser> = emptyList(),
    val filteredUsers: List<InvitableUser> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

fun User.toInvitableUser() = InvitableUser(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl
)
