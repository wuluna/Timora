package cs.uwaterloo.timora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs.uwaterloo.domain.entity.User
import cs.uwaterloo.domain.model.UserModel
import cs.uwaterloo.timora.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userModel: UserModel,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private var pristineUser: User? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = authRepository.getCurrentUser()
                _user.value = currentUser
                if (currentUser == null) {
                    _error.value = "Failed to load user data"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChange(newName: String) {
        _user.value?.let { currentUser ->
            val userToUpdate = currentUser.copy()
            userToUpdate.updateName(newName)
            _user.value = userToUpdate
        }
    }

    fun onBioChange(newBio: String) {
        _user.value?.let { currentUser ->
            val userToUpdate = currentUser.copy()
            userToUpdate.updateBio(newBio)
            _user.value = userToUpdate
        }
    }

    fun startEditing() {
        pristineUser = _user.value?.copy()
    }

    fun cancelEditing() {
        pristineUser?.let {
            _user.value = it
            pristineUser = null
        }
    }

    fun submitChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _user.value?.let { currentUser ->
                    userModel.updateUser(currentUser)
                    pristineUser = null
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = "Failed to update user: ${e.message}"
            }
        }
    }
}
