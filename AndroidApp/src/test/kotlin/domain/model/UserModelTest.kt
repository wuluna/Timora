package cs.uwaterloo.domain.model

import cs.uwaterloo.data.mock.MockDb
import cs.uwaterloo.domain.entity.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserModelTest {
    private lateinit var mockDb: MockDb
    private lateinit var userModel: UserModel

    @BeforeEach
    fun setup() {
        mockDb = MockDb()
        mockDb.clear()
        userModel = UserModel(
            userDao = mockDb.userDao,
            eventDao = mockDb.eventDao,
            invitationDao = mockDb.invitationDao
        )
    }

    @Test
    fun `createUser successful case`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )

            val created = userModel.createUser(user)

            assertNotNull(created)
            assertEquals("user1", created.id)
            assertEquals("test@example.com", created.email)
            assertEquals("Test User", created.name)
        }
    }

    @Test
    fun `getUserById returns user if exists`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userModel.createUser(user)

            val retrieved = userModel.getUserById("user1")

            assertNotNull(retrieved)
            assertEquals("user1", retrieved.id)
            assertEquals("test@example.com", retrieved.email)
        }
    }

    @Test
    fun `getUserByEmail returns user if exists`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userModel.createUser(user)

            val retrieved = userModel.getUserByEmail("test@example.com")

            assertNotNull(retrieved)
            assertEquals("user1", retrieved.id)
            assertEquals("test@example.com", retrieved.email)
        }
    }

    @Test
    fun `updateUser successful`() {
        runBlocking {
            val user = User(
                id = "user1",
                email = "test@example.com",
                name = "Test User",
                avatarUrl = "https://example.com/avatar.jpg"
            )
            userModel.createUser(user)

            user.updateBio("New bio")
            userModel.updateUser(user)

            val updated = userModel.getUserById("user1")
            assertNotNull(updated)
            assertEquals("New bio", updated.bio)
        }
    }
}
