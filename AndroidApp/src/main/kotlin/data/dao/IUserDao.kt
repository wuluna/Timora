package cs.uwaterloo.data.dao

import cs.uwaterloo.domain.entity.User

/**
 * Data Access Object interface for User operations.
 */
interface IUserDao {
    suspend fun create(user: User): User

    suspend fun getById(id: String): User?

    suspend fun getByEmail(email: String): User?

    suspend fun update(user: User): User

    suspend fun delete(id: String)

    suspend fun getAllUsers(): List<User>
}
