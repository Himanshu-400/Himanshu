package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USER PROFILE OPERATIONS ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUser(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserFlow(email: String): Flow<User?>

    @Query("SELECT * FROM users ORDER BY xp DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)


    // --- COURSE OPERATIONS ---
    @Query("SELECT * FROM courses ORDER BY id DESC")
    fun getAllCoursesFlow(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: Int): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Int)


    // --- MOCK TEST OPERATIONS ---
    @Query("SELECT * FROM mock_tests ORDER BY id DESC")
    fun getAllMockTestsFlow(): Flow<List<MockTest>>

    @Query("SELECT * FROM mock_tests WHERE id = :id LIMIT 1")
    suspend fun getMockTestById(id: Int): MockTest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTest)

    @Query("DELETE FROM mock_tests WHERE id = :id")
    suspend fun deleteMockTestById(id: Int)


    // --- LIVE CLASS OPERATIONS ---
    @Query("SELECT * FROM live_classes ORDER BY id DESC")
    fun getAllLiveClassesFlow(): Flow<List<LiveClass>>

    @Query("SELECT * FROM live_classes WHERE id = :id LIMIT 1")
    suspend fun getLiveClassById(id: Int): LiveClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveClass(liveClass: LiveClass)

    @Query("DELETE FROM live_classes WHERE id = :id")
    suspend fun deleteLiveClassById(id: Int)


    // --- NOTIFICATION OPERATIONS ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<StudyNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: StudyNotification)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()


    // --- OMEGA MASTER AI LOGS ---
    @Query("SELECT * FROM omega_logs ORDER BY timestamp DESC")
    fun getAllOmegaLogsFlow(): Flow<List<OmegaLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOmegaLog(log: OmegaLog)
}
