package cs.uwaterloo.domain.model

import cs.uwaterloo.data.dao.IEventDao
import cs.uwaterloo.data.dao.IInvitationDao
import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.domain.entity.User

/**
 * Model class that coordinates user-related operations.
 * Orchestrates between domain entities (business logic) and DAOs (data persistence).
 */
class UserModel(
    private val userDao: IUserDao,
    private val eventDao: IEventDao,
    private val invitationDao: IInvitationDao
) {
    suspend fun createUser(user: User): User {
        return userDao.create(user)
    }

    suspend fun getUserById(id: String): User? {
        return userDao.getById(id)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }
}
