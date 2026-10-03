package cs.uwaterloo.data.mock

import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.domain.entity.User

/**
 * Mock implementation of IUserDao for testing.
 */
class MockUserDao : IUserDao {
    private val users = mutableMapOf<String, User>()

    fun clear() {
        users.clear()
    }

    override suspend fun create(user: User): User {
        if (users.containsKey(user.id)) {
            throw IllegalArgumentException("User with ID ${user.id} already exists")
        }
        users[user.id] = user
        return user
    }

    override suspend fun getById(id: String): User? {
        return users[id]
    }

    override suspend fun getByEmail(email: String): User? {
        return users.values.find { it.email == email }
    }

    override suspend fun update(user: User): User {
        if (!users.containsKey(user.id)) {
            throw IllegalArgumentException("User with ID ${user.id} does not exist")
        }
        users[user.id] = user
        return user
    }

    override suspend fun delete(id: String) {
        users.remove(id)
    }

    override suspend fun getAllUsers(): List<User> {
        return users.values.toList()
    }
}
