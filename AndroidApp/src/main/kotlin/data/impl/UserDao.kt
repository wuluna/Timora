@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package cs.uwaterloo.data.impl

import cs.uwaterloo.data.dao.IUserDao
import cs.uwaterloo.data.db.IDb
import cs.uwaterloo.domain.entity.User
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of IUserDao.
 */
class UserDao(private val db: IDb) : IUserDao {

    @Serializable
    private data class UserDTO(
        val id: String,
        val email: String,
        val name: String,
        val avatar_url: String,
        val bio: String = "",
        val created_at: String? = null,
        val updated_at: String? = null
    )

    override suspend fun create(user: User): User {
        val dto = UserDTO(
            id = user.id,
            email = user.email,
            name = user.name,
            avatar_url = user.avatarUrl,
            bio = user.bio
        )
        db.client.from("users").insert(dto)
        return user
    }

    override suspend fun getById(id: String): User? {
        val result = db.client.from("users")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<UserDTO>()

        return result?.let { dto ->
            User(
                id = dto.id,
                email = dto.email,
                name = dto.name,
                avatarUrl = dto.avatar_url,
                bio = dto.bio,
                hostedEventIds = emptySet(),
                attendedEventIds = emptySet(),
                invitationList = emptySet()
            )
        }
    }

    override suspend fun getByEmail(email: String): User? {
        val result = db.client.from("users")
            .select {
                filter { eq("email", email) }
            }
            .decodeSingleOrNull<UserDTO>()

        return result?.let { dto ->
            User(
                id = dto.id,
                email = dto.email,
                name = dto.name,
                avatarUrl = dto.avatar_url,
                bio = dto.bio,
                hostedEventIds = emptySet(),
                attendedEventIds = emptySet(),
                invitationList = emptySet()
            )
        }
    }

    override suspend fun update(user: User): User {
        val dto = UserDTO(
            id = user.id,
            email = user.email,
            name = user.name,
            avatar_url = user.avatarUrl,
            bio = user.bio
        )
        db.client.from("users").update(dto) {
            filter { eq("id", user.id) }
        }
        return user
    }

    override suspend fun delete(id: String) {
        db.client.from("users").delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun getAllUsers(): List<User> {
        val results = db.client.from("users")
            .select()
            .decodeList<UserDTO>()

        return results.map { dto ->
            User(
                id = dto.id,
                email = dto.email,
                name = dto.name,
                avatarUrl = dto.avatar_url,
                bio = dto.bio,
                hostedEventIds = emptySet(),
                attendedEventIds = emptySet(),
                invitationList = emptySet()
            )
        }
    }
}
