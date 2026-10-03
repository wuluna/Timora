package cs.uwaterloo.data.mock

/**
 * Mock database that holds all mock DAOs for testing.
 * Provides a centralized way to access all DAOs and clear data between tests.
 */
class MockDb {
    val userDao: MockUserDao = MockUserDao()
    val eventDao: MockEventDao
    val attendeeDao: MockAttendeeDao = MockAttendeeDao()
    val invitationDao: MockInvitationDao = MockInvitationDao()
    val announcementDao: MockAnnouncementDao = MockAnnouncementDao()
    val mutableCategoryDao: MockMutableCategoryDao = MockMutableCategoryDao()
    val immutableCategoryDao: MockImmutableCategoryDao = MockImmutableCategoryDao()

    init {
        eventDao = MockEventDao(
            userDao,
            attendeeDao,
            announcementDao,
            mutableCategoryDao,
            immutableCategoryDao
        )
    }

    fun clear() {
        userDao.clear()
        eventDao.clear()
        attendeeDao.clear()
        invitationDao.clear()
        announcementDao.clear()
        mutableCategoryDao.clear()
        immutableCategoryDao.clear()
    }
}
