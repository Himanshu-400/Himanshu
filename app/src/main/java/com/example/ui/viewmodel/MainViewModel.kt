package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    // --- State Holders ---
    val allUsers = dao.getAllUsersFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCourses = dao.getAllCoursesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMockTests = dao.getAllMockTestsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allLiveClasses = dao.getAllLiveClassesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotifications = dao.getAllNotificationsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allOmegaLogs = dao.getAllOmegaLogsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Session ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentTheme = MutableStateFlow(com.example.ui.theme.StudyNovaThemeStyle.COSMIC_DARK)
    val currentTheme: StateFlow<com.example.ui.theme.StudyNovaThemeStyle> = _currentTheme.asStateFlow()

    // --- Student Feature States ---
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    private val _activeLesson = MutableStateFlow<Lesson?>(null)
    val activeLesson: StateFlow<Lesson?> = _activeLesson.asStateFlow()

    // Simulated PDF download tracking (courseId to download percentage)
    private val _downloadProgress = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<Int, Int>> = _downloadProgress.asStateFlow()

    // Bookmark States
    private val _bookmarkedLessons = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedLessons: StateFlow<Set<String>> = _bookmarkedLessons.asStateFlow()

    // --- Custom Premium Video Player States ---
    val playbackSpeed = MutableStateFlow(1.0f)
    val playerVolume = MutableStateFlow(0.8f) // 0.0 to 1.0
    val playerBrightness = MutableStateFlow(0.7f) // 0.0 to 1.0
    val isFullscreen = MutableStateFlow(false)

    // --- Live Class Interaction States ---
    private val _activeLiveClass = MutableStateFlow<LiveClass?>(null)
    val activeLiveClass: StateFlow<LiveClass?> = _activeLiveClass.asStateFlow()

    private val _liveClassChat = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    val liveClassChat: StateFlow<List<LiveChatMessage>> = _liveClassChat.asStateFlow()

    private val _raisedHands = MutableStateFlow<Set<String>>(emptySet()) // set of student emails
    val raisedHands: StateFlow<Set<String>> = _raisedHands.asStateFlow()

    private val _pollVotesA = MutableStateFlow(0)
    val pollVotesA: StateFlow<Int> = _pollVotesA.asStateFlow()

    private val _pollVotesB = MutableStateFlow(0)
    val pollVotesB: StateFlow<Int> = _pollVotesB.asStateFlow()

    private val _hasVotedCurrentPoll = MutableStateFlow(false)
    val hasVotedCurrentPoll: StateFlow<Boolean> = _hasVotedCurrentPoll.asStateFlow()

    // --- Mock Test Runner States ---
    private val _activeTest = MutableStateFlow<MockTest?>(null)
    val activeTest: StateFlow<MockTest?> = _activeTest.asStateFlow()

    private val _testQuestions = MutableStateFlow<List<McqQuestion>>(emptyList())
    val testQuestions: StateFlow<List<McqQuestion>> = _testQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // selected option index for each question
    private val _testAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) 
    val testAnswers: StateFlow<Map<Int, Int>> = _testAnswers.asStateFlow()

    private val _testSecondsRemaining = MutableStateFlow(0)
    val testSecondsRemaining: StateFlow<Int> = _testSecondsRemaining.asStateFlow()

    private var testTimerJob: Job? = null

    private val _testResult = MutableStateFlow<TestResult?>(null)
    val testResult: StateFlow<TestResult?> = _testResult.asStateFlow()

    // --- AI Doubt Assistant ---
    private val _doubtConversation = MutableStateFlow<List<ChatMessage>>(emptyList())
    val doubtConversation: StateFlow<List<ChatMessage>> = _doubtConversation.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    // --- SUPREME AI Command Terminal States ---
    private val _omegaConsoleOutput = MutableStateFlow<List<String>>(listOf("SYSTEM ONLINE - SUPREME AI CORE AGENT INITIALIZED.", "Awaiting commands from Master Access: dhinwabrothers125@gmail.com"))
    val omegaConsoleOutput: StateFlow<List<String>> = _omegaConsoleOutput.asStateFlow()

    private val _isOmegaProcessing = MutableStateFlow(false)
    val isOmegaProcessing: StateFlow<Boolean> = _isOmegaProcessing.asStateFlow()

    init {
        // Pre-populate data securely
        viewModelScope.launch {
            insertInitialDataIfNeeded()
        }
    }

    // --- THEME CHANGING ---
    fun changeTheme(style: com.example.ui.theme.StudyNovaThemeStyle) {
        _currentTheme.value = style
    }

    // --- AUTHENTICATION ---
    fun login(email: String, passwordText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            // Fixed Admin Account System Rule
            if (email.lowercase().trim() == "dhinwabrothers125@gmail.com") {
                if (passwordText == "himanshu@#9680") {
                    val user = dao.getUser(email) ?: User(
                        email = email,
                        passwordHash = "himanshu@#9680",
                        name = "Himanshu Dhinwa (Admin)",
                        role = "admin",
                        xp = 9999,
                        coins = 5000,
                        level = 99
                    )
                    dao.insertUser(user)
                    _currentUser.value = user
                    onResult(true, "Master Access Control Activated. Welcome, Admin!")
                    triggerStreakTracker(user)
                    return@launch
                } else {
                    onResult(false, "Unauthorized Admin attempt: Invalid Credentials.")
                    return@launch
                }
            }

            // Normal Student or Teacher Check
            val existingUser = dao.getUser(email.trim())
            if (existingUser != null) {
                if (existingUser.passwordHash == passwordText) {
                    if (existingUser.isBlocked) {
                        onResult(false, "Access Denied: Your account has been temporarily blocked by SUPREME AI Security.")
                        return@launch
                    }
                    if (existingUser.role == "teacher" && !existingUser.isApproved) {
                        onResult(false, "Pending: Your Teacher request of registration is awaiting verification by Admin.")
                        return@launch
                    }
                    _currentUser.value = existingUser
                    onResult(true, "Login Successful! Welcome, ${existingUser.name}.")
                    triggerStreakTracker(existingUser)
                } else {
                    onResult(false, "Invalid password. Please check your credentials.")
                }
            } else {
                // If student tries login, provide an auto-account registration to make it ultra realistic & fully functional!
                // This eliminates any Signup block error
                val newStudent = User(
                    email = email.trim(),
                    passwordHash = passwordText,
                    name = email.substringBefore("@").replaceFirstChar { it.titlecase() },
                    role = "student",
                    xp = 100,
                    coins = 10,
                    streak = 1,
                    level = 1,
                    lastActiveDate = getTodayDateString()
                )
                dao.insertUser(newStudent)
                _currentUser.value = newStudent
                onResult(true, "New Student enrolled successfully!")
                triggerStreakTracker(newStudent)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedCourse.value = null
        _activeLesson.value = null
        _activeLiveClass.value = null
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private suspend fun triggerStreakTracker(user: User) {
        val today = getTodayDateString()
        if (user.lastActiveDate != today) {
            val updatedUser = user.copy(
                streak = user.streak + 1,
                xp = user.xp + 50, // Daily rewards 50 XP
                coins = user.coins + 10,
                lastActiveDate = today
            )
            dao.updateUser(updatedUser)
            _currentUser.value = updatedUser
            
            // Dispatch a beautiful daily streak alert notification
            val notif = StudyNotification(
                title = "Streak Extended! 🔥 Lvl ${updatedUser.streak}",
                message = "Hooray! You logged in today. +50 XP and +10 Coins, Keep StudyNova-ing!",
                category = "achievement"
            )
            dao.insertNotification(notif)
        }
    }

    // --- DOUBT ASSISTANT FLOW ---
    fun sendDoubt(message: String) {
        val userMsg = ChatMessage(sender = "student", text = message)
        _doubtConversation.value = _doubtConversation.value + userMsg
        
        _isAILoading.value = true
        viewModelScope.launch {
            val userProfile = currentUser.value
            val sysPrompt = """
                You are StudyNova's premium visual learning AI Assistant (Nova AI). 
                The student's name is ${userProfile?.name ?: "Learner"}. They have XP ${userProfile?.xp} and are Level ${userProfile?.level}.
                Provide accurate, visually structural educational help, using lists, formulas, and structured markdown. Keep it encouraging, fun, like international coaching platforms Allen, Physics Wallah, and Unacademy mixed!
                Understand Hinglish and Hindi queries. Keep explanations extremely deep yet simplified.
            """.trimIndent()
            
            val aiReply = GeminiService.generateResponse(message, sysPrompt)
            val aiMsg = ChatMessage(sender = "ai", text = aiReply)
            _doubtConversation.value = _doubtConversation.value + aiMsg
            _isAILoading.value = false

            // Reward 5 XP for asking educational doubt!
            rewardXpCoins(5, 1)
        }
    }

    private fun rewardXpCoins(xpAmount: Int, coinAmount: Int) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val newXp = user.xp + xpAmount
                val newLevel = (newXp / 500) + 1
                val updated = user.copy(
                    xp = newXp,
                    coins = user.coins + coinAmount,
                    level = if (newLevel > user.level) {
                        // Dispatch achievement alert and trigger badge awards
                        launch {
                            dao.insertNotification(StudyNotification(
                                title = "Level Up! 🎯 Level $newLevel reached",
                                message = "Astonishing work, ${user.name}! You unlocked new academic badges and +50 bonus coins.",
                                category = "achievement"
                            ))
                        }
                        newLevel
                    } else user.level
                )
                dao.updateUser(updated)
                _currentUser.value = updated
            }
        }
    }

    // --- PDF notes downloader simulator ---
    fun downloadPdfNotes(courseId: Int, fileUrl: String, fileName: String) {
        viewModelScope.launch {
            if (_downloadProgress.value[courseId] != null && _downloadProgress.value[courseId] == 100) {
                // Already downloaded, skip
                return@launch
            }
            
            // Simulating authentic continuous progress
            for (progress in 5..100 step 15) {
                _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                    put(courseId, progress)
                }
                delay(300)
            }
            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                put(courseId, 100)
            }

            // Earn points for offline content usage
            rewardXpCoins(15, 2)

            // Notifications log
            dao.insertNotification(StudyNotification(
                title = "Download Complete: $fileName 💾",
                message = "The notes PDF file for course has been securely cached in StudyNova offline storage vault.",
                category = "new_lecture"
            ))
        }
    }

    // --- BOOKMARKS ---
    fun toggleBookmark(lessonId: String) {
        val currentSet = _bookmarkedLessons.value
        if (currentSet.contains(lessonId)) {
            _bookmarkedLessons.value = currentSet - lessonId
        } else {
            _bookmarkedLessons.value = currentSet + lessonId
        }
    }

    // --- MOCK TEST ENGINE UNIT ---
    fun startMockTest(test: MockTest) {
        _activeTest.value = test
        
        // Parse the questions
        val qList = mutableListOf<McqQuestion>()
        try {
            val array = JSONArray(test.questionsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                qList.add(
                    McqQuestion(
                        id = obj.optInt("id", i + 1),
                        questionText = obj.optString("questionText"),
                        optionA = obj.optString("optionA"),
                        optionB = obj.optString("optionB"),
                        optionC = obj.optString("optionC"),
                        optionD = obj.optString("optionD"),
                        correctAnswerIndex = obj.optInt("correctAnswerIndex"),
                        explanation = obj.optString("explanation", "Explaining physics mechanism.")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error parsing mock test questions", e)
        }
        
        _testQuestions.value = qList
        _currentQuestionIndex.value = 0
        _testAnswers.value = emptyMap()
        _testResult.value = null
        _testSecondsRemaining.value = test.durationMinutes * 60

        // Start timer
        testTimerJob?.cancel()
        testTimerJob = viewModelScope.launch {
            while (_testSecondsRemaining.value > 0) {
                delay(1000)
                _testSecondsRemaining.value -= 1
            }
            submitMockTest() // Auto-submit when time is up
        }
    }

    fun answerQuestion(questionIndex: Int, selectedOption: Int) {
        _testAnswers.value = _testAnswers.value.toMutableMap().apply {
            put(questionIndex, selectedOption)
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _testQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun submitMockTest() {
        testTimerJob?.cancel()
        val questions = _testQuestions.value
        val answers = _testAnswers.value
        
        if (questions.isEmpty()) return

        var correctAnswers = 0
        var unattempted = 0
        val details = mutableListOf<QuestionResultDetail>()

        for (i in questions.indices) {
            val q = questions[i]
            val chosen = answers[i]
            if (chosen == null) {
                unattempted++
                details.add(QuestionResultDetail(q.questionText, -1, q.correctAnswerIndex, q.explanation))
            } else {
                if (chosen == q.correctAnswerIndex) {
                    correctAnswers++
                }
                details.add(QuestionResultDetail(q.questionText, chosen, q.correctAnswerIndex, q.explanation))
            }
        }

        val totalQuestions = questions.size
        val scoreEarned = correctAnswers * 4 - (totalQuestions - correctAnswers - unattempted) * 1 // JEE marking standard (+4, -1)
        val percentage = (correctAnswers.toFloat() / totalQuestions * 100).toInt()

        val result = TestResult(
            title = _activeTest.value?.title ?: "Assessment Core",
            totalScore = totalQuestions * 4,
            score = if (scoreEarned < 0) 0 else scoreEarned,
            correctCount = correctAnswers,
            incorrectCount = totalQuestions - correctAnswers - unattempted,
            unattemptedCount = unattempted,
            percentage = percentage,
            details = details
        )
        
        _testResult.value = result

        // Reward Gamified assets
        val earnedXp = if (percentage > 50) 100 else 40
        val earnedCoins = if (percentage > 80) 50 else 10
        rewardXpCoins(earnedXp, earnedCoins)

        // Dispatch achievements log
        viewModelScope.launch {
            dao.insertNotification(StudyNotification(
                title = "Mock Complete: Score $scoreEarned points",
                message = "Completed ${result.title} with accuracy of $percentage%. +$earnedXp XP granted.",
                category = "exam_alert"
            ))
        }
    }

    fun closeTestSession() {
        _activeTest.value = null
        _testResult.value = null
    }

    // --- LIVE CLASS STREAMS ---
    fun joinLiveClass(liveClass: LiveClass) {
        _activeLiveClass.value = liveClass
        _hasVotedCurrentPoll.value = false
        _pollVotesA.value = 23
        _pollVotesB.value = 14
        
        // Load messages from json or pre-set standard simulated live chats
        val mockMsgs = mutableListOf<LiveChatMessage>()
        try {
            val array = JSONArray(liveClass.chatJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                mockMsgs.add(
                    LiveChatMessage(
                        senderName = obj.optString("senderName"),
                        senderRole = obj.optString("senderRole"),
                        message = obj.optString("message"),
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            // Add fun realistic simulated chat
            mockMsgs.add(LiveChatMessage("Shubham", "student", "Alakh Sir! Will this be asked in JEE Mains 2027?"))
            mockMsgs.add(LiveChatMessage("Alakh Pandey", "teacher", "Yes Shubham, write down this dynamic equation right now!"))
            mockMsgs.add(LiveChatMessage("Sneha", "student", "Wow explanation is crystal clear, Allen material feels easy now."))
            mockMsgs.add(LiveChatMessage("Karthik", "student", "Let's crack UPSC and JEE with StudyNova 🔥"))
        }
        _liveClassChat.value = mockMsgs
        _raisedHands.value = emptySet()
    }

    fun sendLiveMessage(msg: String) {
        val user = currentUser.value ?: return
        val newMsg = LiveChatMessage(
            senderName = user.name,
            senderRole = user.role,
            message = msg,
            timestamp = System.currentTimeMillis()
        )
        _liveClassChat.value = _liveClassChat.value + newMsg

        // Fast simulated teacher reply to make it feel absolute reality
        viewModelScope.launch {
            delay(1500)
            val teacherName = _activeLiveClass.value?.teacherName ?: "Instructor"
            val responses = listOf(
                "Great point ${user.name}! Keep this energy high.",
                "Excellent observation. Let me write out the math formulation.",
                "Correct! Yes. Focus on the core mechanics.",
                "Absolutely study this chapter properly."
            )
            _liveClassChat.value = _liveClassChat.value + LiveChatMessage(
                senderName = teacherName,
                senderRole = "teacher",
                message = responses.random()
            )
        }
    }

    fun raiseHand() {
        val email = currentUser.value?.email ?: return
        val currentSet = _raisedHands.value
        if (currentSet.contains(email)) {
            _raisedHands.value = currentSet - email
        } else {
            _raisedHands.value = currentSet + email
            // Simulated instant teacher acknowledgement
            viewModelScope.launch {
                delay(1200)
                val response = LiveChatMessage(
                    senderName = _activeLiveClass.value?.teacherName ?: "Instructor",
                    senderRole = "teacher",
                    message = "Student ${currentUser.value?.name} raises hand! Auditing doubt now."
                )
                _liveClassChat.value = _liveClassChat.value + response
            }
        }
    }

    fun votePoll(option: String) {
        if (_hasVotedCurrentPoll.value) return
        _hasVotedCurrentPoll.value = true
        if (option == "A") {
            _pollVotesA.value += 1
        } else {
            _pollVotesB.value += 1
        }
        rewardXpCoins(10, 1) // XP for active participation in live classes
    }

    fun leaveLiveClass() {
        _activeLiveClass.value = null
    }

    // --- COURSE CATALOG SECTIONS (ADMIN DIRECT CONTROL IN ROOM) ---
    fun adminCreateCourse(title: String, desc: String, category: String, banner: String, instructor: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val dummyLessons = listOf(
                Lesson("L1", "Introduction to $title", "Basic definitions and axioms.", "https://example.com/stream1", "https://firebasestorage.googleapis.com/v0/b/study-by-himanshu-dhinwa.appspot.com/o/Introduction_General.pdf", "Lecture_01_Basics.pdf"),
                Lesson("L2", "Core Framework", "Advanced formulas and dynamic graphs.", "https://example.com/stream2", "https://firebasestorage.googleapis.com/v0/b/study-by-himanshu-dhinwa.appspot.com/o/Advanced_Theory.pdf", "Lecture_02_Laws.pdf")
            )
            val jsonLessons = serializeLessons(dummyLessons)
            
            val newC = Course(
                title = title,
                description = desc,
                category = category,
                instructorEmail = user.email,
                instructorName = instructor,
                bannerUrl = banner.ifEmpty { "https://images.unsplash.com/photo-1516321318423-f06f85e504b3" },
                lessonsJson = jsonLessons
            )
            
            dao.insertCourse(newC)
            
            // Broadcast alert to everyone automatically
            val alert = StudyNotification(
                title = "New Course: $title",
                message = "Officially launched by StudyNova editorial board in category $category. Enroll now!",
                category = "new_lecture",
                roleTarget = "all"
            )
            dao.insertNotification(alert)
        }
    }

    fun adminDeleteCourse(courseId: Int) {
        viewModelScope.launch {
            dao.deleteCourseById(courseId)
        }
    }

    fun selectCourseAndOpen(course: Course) {
        _selectedCourse.value = course
        val lessons = deserializeLessons(course.lessonsJson)
        _activeLesson.value = lessons.firstOrNull()
    }

    fun changeActiveLesson(lesson: Lesson) {
        _activeLesson.value = lesson
    }

    // --- ADMIN USER AUDITING CONTROLS ---
    fun toggleUserBlockStatus(email: String) {
        viewModelScope.launch {
            val u = dao.getUser(email) ?: return@launch
            if (u.email == "dhinwabrothers125@gmail.com") return@launch // Master account immune
            val updated = u.copy(isBlocked = !u.isBlocked)
            dao.updateUser(updated)
            
            // Log in console
            insertCustomOmegaLog("BLOCK_STATUS_TOGGLED", "User ${u.name} (Role: ${u.role}) block state set to ${updated.isBlocked} successfully.")
        }
    }

    fun approveTeacherRegistration(email: String) {
        viewModelScope.launch {
            val u = dao.getUser(email) ?: return@launch
            if (u.role == "teacher") {
                val updated = u.copy(isApproved = true)
                dao.updateUser(updated)
                
                // Add default analytics log
                insertCustomOmegaLog("TEACHER_APPROVED", "Teacher account for ${u.name} activated and certified under StudyNova Faculty Board.")
                
                // Alert in system notifications
                dao.insertNotification(StudyNotification(
                    title = "Faculty Approved: Prof. ${u.name} 🎓",
                    message = "We are super honored to welcome a new pioneer directly in our tutoring board! Courses uploaded soon.",
                    category = "live_class"
                ))
            }
        }
    }

    fun adminCreateTeacherRequest(name: String, email: String, pass: String) {
        viewModelScope.launch {
            val newTeacher = User(
                email = email.trim(),
                passwordHash = pass,
                name = name,
                role = "teacher",
                isApproved = true, // Admin created teachers are directly approved!
                xp = 2000,
                level = 10
            )
            dao.insertUser(newTeacher)
            insertCustomOmegaLog("TEACHER_CREATED", "New faculty credential generated: $name ($email). Direct bypass approved.")
        }
    }

    // --- REALTIME ALERTS BROADCAST ---
    fun broadcastGlobalAlert(title: String, body: String, category: String = "omega_broadcast") {
        viewModelScope.launch {
            val notif = StudyNotification(
                title = title,
                message = body,
                category = category,
                roleTarget = "all"
            )
            dao.insertNotification(notif)
            
            insertCustomOmegaLog("NOTIF_BROADCAST", "Global pushing broadcast deployed successfully: $title")
        }
    }

    // --- SUPREME AI AUTO-PILOT SYSTEM DIRECT INTEGRATION ---
    fun runOmegaCommand(command: String) {
        if (command.isBlank()) return
        
        _isOmegaProcessing.value = true
        _omegaConsoleOutput.value = _omegaConsoleOutput.value + ">> $command"
        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "SUPREME AI CORE: Spanning telemetry and compiling generative query..."

        viewModelScope.launch {
            val decision = GeminiService.interpretOmegaCommand(command)
            
            _omegaConsoleOutput.value = _omegaConsoleOutput.value + "🤖 [SUPREME AI DEEP INSIGHT]:"
            _omegaConsoleOutput.value = _omegaConsoleOutput.value + decision.aiAnalysis
            _omegaConsoleOutput.value = _omegaConsoleOutput.value + "EXECUTING ACTION STATE: ${decision.actionRequested}"

            // Execute Autopilot Room insertions
            when (decision.actionRequested) {
                "CREATE_TEST" -> {
                    try {
                        val testTitle = decision.targetName.ifEmpty { "AI Generated MCQ Assessment" }
                        val newTest = MockTest(
                            title = testTitle,
                            category = decision.category.ifEmpty { "General" },
                            questionsJson = decision.createdPayload,
                            isPublished = true
                        )
                        dao.insertMockTest(newTest)
                        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "✅ Successfully generated and published new Mock Test: '$testTitle'."
                        
                        dao.insertNotification(StudyNotification(
                            title = "New AI Test: $testTitle 📝",
                            message = "Generated autonomously by our SUPREME AI Autopilot block! Test your accuracy on ${decision.category}.",
                            category = "exam_alert"
                        ))
                    } catch (e: Exception) {
                        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "❌ Failed compiling generated questions array: ${e.localizedMessage}"
                    }
                }
                "SEND_NOTIFICATION" -> {
                    try {
                        val json = JSONObject(decision.createdPayload)
                        val title = json.optString("title", "StudyNova Special Announcement")
                        val message = json.optString("message", "Keep studying and tracking your level progressions!")
                        
                        val broad = StudyNotification(
                            title = title,
                            message = message,
                            category = "omega_broadcast",
                            roleTarget = "all"
                        )
                        dao.insertNotification(broad)
                        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "✅ System broadcast deployed dynamically: '$title'."
                    } catch (e: Exception) {
                        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "❌ Parsing notification payload failed. Dispatching plain alert..."
                        dao.insertNotification(StudyNotification(
                            title = "AI Prompt Broadcast",
                            message = "SUPREME AI: " + decision.aiAnalysis.take(150) + "...",
                            category = "omega_broadcast"
                        ))
                    }
                }
                "CREATE_COURSE" -> {
                    try {
                        val json = JSONObject(decision.createdPayload)
                        val title = json.optString("title", decision.targetName)
                        val desc = json.optString("description", "Premium dynamic course structured by SUPREME AI.")
                        val cat = json.optString("category", decision.category)
                        
                        val lessonsArr = json.optJSONArray("lessons")
                        val lessonsList = mutableListOf<Lesson>()
                        if (lessonsArr != null) {
                            for (x in 0 until lessonsArr.length()) {
                                val item = lessonsArr.getJSONObject(x)
                                lessonsList.add(Lesson(
                                    id = "AI$x",
                                    title = item.optString("title", "Lecture $x"),
                                    description = item.optString("description", "Aesthetic conceptual explanations."),
                                    videoUrl = item.optString("videoUrl", "https://example.com/video")
                                ))
                            }
                        } else {
                            lessonsList.add(Lesson("AI0", "Core Concepts Overview", "First principal dynamics.", "https://example.com"))
                        }

                        val nCourse = Course(
                            title = title,
                            description = desc,
                            category = cat,
                            instructorEmail = "dhinwabrothers125@gmail.com",
                            instructorName = "SUPREME AI Core Master Engine",
                            lessonsJson = serializeLessons(lessonsList)
                        )
                        dao.insertCourse(nCourse)
                        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "✅ Cyber Course '$title' is active."
                        
                        dao.insertNotification(StudyNotification(
                            title = "New Cyber Course Active: $title 📚",
                            message = "An complete AI structured path is open inside StudyNova portfolio.",
                            category = "new_lecture"
                        ))
                    } catch (e: Exception) {
                        _omegaConsoleOutput.value = _omegaConsoleOutput.value + "❌ Autopilot failed creating customized Course payload. Injecting standard structure..."
                    }
                }
                "GENERAL_ANALYTICS" -> {
                    // Optimized caches, database defragmentation, streak boosts simulated
                    _omegaConsoleOutput.value = _omegaConsoleOutput.value + "🛡️ Dynamic memory buffer optimized. 0 issues detected."
                }
            }

            // Save trace to Omega Logs DB
            val log = OmegaLog(
                command = command,
                aiResponse = decision.aiAnalysis
            )
            dao.insertOmegaLog(log)
            _isOmegaProcessing.value = false
        }
    }

    private suspend fun insertCustomOmegaLog(cmd: String, msg: String) {
        dao.insertOmegaLog(OmegaLog(
            command = cmd,
            aiResponse = msg
        ))
    }

    // --- JSON SERIALIZERS / DESERIALIZERS FOR INT AND LIST CORES ---
    private fun serializeLessons(lessons: List<Lesson>): String {
        val array = JSONArray()
        for (l in lessons) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("title", l.title)
            obj.put("description", l.description)
            obj.put("videoUrl", l.videoUrl)
            obj.put("pdfUrl", l.pdfUrl)
            obj.put("pdfName", l.pdfName)
            obj.put("durationSeconds", l.durationSeconds)
            obj.put("isBookmarked", l.isBookmarked)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeLessons(json: String): List<Lesson> {
        val list = mutableListOf<Lesson>()
        if (json.isBlank() || json == "[]") return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Lesson(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        description = obj.optString("description"),
                        videoUrl = obj.optString("videoUrl"),
                        pdfUrl = obj.optString("pdfUrl"),
                        pdfName = obj.optString("pdfName"),
                        durationSeconds = obj.optInt("durationSeconds", 600)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error deserializing lessons", e)
        }
        return list
    }

    // --- INITIAL DATA SEEDING ENGINE ON CORE INITIALIZATION ---
    private suspend fun insertInitialDataIfNeeded() {
        // Enforce only one fixed Admin account
        val rootAdmin = dao.getUser("dhinwabrothers125@gmail.com")
        if (rootAdmin == null) {
            dao.insertUser(User(
                email = "dhinwabrothers125@gmail.com",
                passwordHash = "himanshu@#9680",
                name = "Himanshu Dhinwa (Admin)",
                role = "admin",
                xp = 18000,
                coins = 2400,
                streak = 9,
                level = 36
            ))
        }

        // Test student
        if (dao.getUser("student@studynova.com") == null) {
            dao.insertUser(User(
                email = "student@studynova.com",
                passwordHash = "student123",
                name = "Aman Sharma",
                role = "student",
                xp = 850,
                coins = 40,
                streak = 4,
                level = 2,
                lastActiveDate = "2026-05-23"
            ))
        }

        // Test teachers
        if (dao.getUser("alakh@studynova.com") == null) {
            dao.insertUser(User(email = "alakh@studynova.com", passwordHash = "test123", name = "Alakh Pandey Sir", role = "teacher", isApproved = true))
        }
        if (dao.getUser("sofia@studynova.com") == null) {
            dao.insertUser(User(email = "sofia@studynova.com", passwordHash = "test123", name = "Sofia Smith", role = "teacher", isApproved = true))
        }
        if (dao.getUser("hcverma@studynova.com") == null) {
            dao.insertUser(User(email = "hcverma@studynova.com", passwordHash = "test123", name = "Prof. H.C. Verma", role = "teacher", isApproved = false)) // Pending
        }

        // Check if courses are seeded
        dao.getAllCoursesFlow().first().let { coursesList ->
            if (coursesList.isEmpty()) {
                // Course 1: JEE Advanced Physics Elite - Rotational Kinematics
                val c1Lessons = listOf(
                    Lesson("L1_1", "Moment Of Inertia: Ring & Cylinder", "Deep derivation of Inertial properties of rings, cylinders using integration.", "https://example.com/phys1.mp4", "https://firebasestorage.googleapis.com/v0/b/study-by-himanshu-dhinwa.appspot.com/o/JEE_Rotational_Lecture01.pdf", "Lecture_01_Inertia.pdf"),
                    Lesson("L1_2", "Angular Momentum Conservation", "Explore gyroscopes, dynamic rotations, and angular stability.", "https://example.com/phys2.mp4", "https://firebasestorage.googleapis.com/v0/b/study-by-himanshu-dhinwa.appspot.com/o/Ang_Momentum_Concepts.pdf", "Lecture_02_Momentum.pdf"),
                    Lesson("L1_3", "Pure Rolling Mechanics", "Kinematic equations governing slip/no slip friction limits on dynamic inclines.", "https://example.com/phys3.mp4")
                )
                dao.insertCourse(Course(
                    title = "Rotational Mechanics for JEE Advanced",
                    description = "Master high-score Physics concepts from basic equilibrium up to rigid-body kinetic dynamics under Alakh Sir's mentoring.",
                    category = "JEE",
                    instructorEmail = "alakh@studynova.com",
                    instructorName = "Alakh Pandey Sir",
                    bannerUrl = "https://images.unsplash.com/photo-1616469829581-73993eb86b02?w=800",
                    lessonsJson = serializeLessons(c1Lessons),
                    isPremium = false
                ))

                // Course 2: NEET Biology Masterclass - Human Genetics
                val c2Lessons = listOf(
                    Lesson("L2_1", "Mendelian Genetics Principles", "Understand homozygous, heterozygous traits, and inheritance square calculations.", "https://example.com/bio1.mp4", "https://firebasestorage.googleapis.com/v0/b/study-by-himanshu-dhinwa.appspot.com/o/NEET_Genetics_Mendel.pdf", "NEET_Mendelian_Axioms.pdf"),
                    Lesson("L2_2", "DNA Recombination & Crossing", "Interactive genetic cross examinations with visual animation guides.", "https://example.com/bio2.mp4")
                )
                dao.insertCourse(Course(
                    title = "NEET Biology: Human Genetic Code",
                    description = "Score a solid 360/360 in NEET Biology. Dive deep into chromosomes, DNA helix mapping, and Mendelian squares.",
                    category = "NEET",
                    instructorEmail = "sofia@studynova.com",
                    instructorName = "Dr. Sofia Smith",
                    bannerUrl = "https://images.unsplash.com/photo-1532187643603-ba119ca4109e?w=800",
                    lessonsJson = serializeLessons(c2Lessons),
                    isPremium = true
                ))

                // Course 3: Coding Masterclass
                val c3Lessons = listOf(
                    Lesson("L3_1", "Jetpack Compose: Drawing on Canvas", "Custom graphic rendering in android, paint strokes, radial glowing elements.", "https://example.com/compose_canvas.mp4", "", "Compose_Glow_Cheatsheet.pdf"),
                    Lesson("L3_2", "Advanced Room Transaction Hooks", "Handling offline caching, stateflows, and fallback repositories.", "https://example.com/room_flow.mp4")
                )
                dao.insertCourse(Course(
                    title = "Premium Android Development & Jetpack Compose",
                    description = "Learn production-ready declarative UI crafting with neat state variables, custom modifiers, and responsive design layouts.",
                    category = "Coding Courses",
                    instructorEmail = "dhinwabrothers125@gmail.com",
                    instructorName = "Master Admin (Himanshu)",
                    bannerUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800",
                    lessonsJson = serializeLessons(c3Lessons),
                    isPremium = false
                ))
            }
        }

        // Mock Tests Seeding
        dao.getAllMockTestsFlow().first().let { testsList ->
            if (testsList.isEmpty()) {
                val qList1 = listOf(
                    McqQuestion(1, "What is the Moment of Inertia of a uniform solid cylinder of mass M and radius R about its central axis?", "MR^2", "1/2 MR^2", "2/5 MR^2", "1/12 MR^2", 1, "The inertial moment about its longitudinal line is 1/2 M*R^2."),
                    McqQuestion(2, "If a sphere and solid cylinder roll down an incline without slipping, which reaches the bottom first?", "Solid Sphere", "Solid Cylinder", "Both reach together", "Insufficient structural details", 0, "A solid sphere has less relative inertia coefficient (2/5 < 1/2) hence accelerates faster."),
                    McqQuestion(3, "Kinetic Energy of a pure rolling disk is composed of what parts?", "Translation only", "Rotation only", "Both Translational and Rotational velocity", "Static friction heat", 2, "Rolling motion of any rigid profile equals the sum of translational energy and centroidal rotational kinetic energy.")
                )
                dao.insertMockTest(MockTest(
                    title = "Rotational Equilibrium Grand Practice Test",
                    category = "JEE",
                    durationMinutes = 5,
                    questionsJson = serializeMockMcqs(qList1)
                ))

                val qList2 = listOf(
                    McqQuestion(1, "Which phenotypic ratio is expected in a Mendelian dihybrid inheritance ratio?", "3:1", "1:2:1", "9:3:3:1", "1:1:1:1", 2, "Mendel's second law of independent assortment reveals a 9:3:3:1 phenotype ratio."),
                    McqQuestion(2, "Who coined the term inheritance 'Gene' originally?", "Gregor Mendel", "Wilhelm Johannsen", "Thomas Morgan", "Watson & Crick", 1, "Johannsen coined 'gene' in 1909 to denote discrete hereditary units.")
                )
                dao.insertMockTest(MockTest(
                    title = "Mendelian Genetics and Mutation Quiz",
                    category = "NEET",
                    durationMinutes = 4,
                    questionsJson = serializeMockMcqs(qList2)
                ))
            }
        }

        // Live Classes Seeding
        dao.getAllLiveClassesFlow().first().let { livesList ->
            if (livesList.isEmpty()) {
                dao.insertLiveClass(LiveClass(
                    title = "Rotational Mechanics: Hardest JEE Adv PYQs",
                    category = "JEE",
                    teacherEmail = "alakh@studynova.com",
                    teacherName = "Alakh Pandey Sir",
                    status = "live",
                    watchers = 3200,
                    chatJson = "[]"
                ))
                
                dao.insertLiveClass(LiveClass(
                    title = "DNA Double Helix: Live 3D Model Building",
                    category = "NEET",
                    teacherEmail = "sofia@studynova.com",
                    teacherName = "Dr. Sofia Smith",
                    status = "upcoming",
                    scheduledAt = "Tonight at 8:00 PM",
                    watchers = 0,
                    chatJson = "[]"
                ))
            }
        }

        // Notification seeds
        dao.getAllNotificationsFlow().first().let { currentNotifs ->
            if (currentNotifs.isEmpty()) {
                dao.insertNotification(StudyNotification(
                    title = "Welcome to StudyNova Space! 🚀",
                    message = "Experience the absolute top-tier learning portal. Enroll, solve daily streaks to level up and interact with Nova AI doubts resolver.",
                    category = "achievement"
                ))
                dao.insertNotification(StudyNotification(
                    title = "Live Class Alert: Physics 🚨",
                    message = "Alakh Pandey Sir is solving toughest Rotational Kinematic concepts. Join live chat room now!",
                    category = "live_class"
                ))
            }
        }
    }

    private fun serializeMockMcqs(mcqs: List<McqQuestion>): String {
        val array = JSONArray()
        for (q in mcqs) {
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("questionText", q.questionText)
            obj.put("optionA", q.optionA)
            obj.put("optionB", q.optionB)
            obj.put("optionC", q.optionC)
            obj.put("optionD", q.optionD)
            obj.put("correctAnswerIndex", q.correctAnswerIndex)
            obj.put("explanation", q.explanation)
            array.put(obj)
        }
        return array.toString()
    }
}

// --- Data Models for Chats and Testing Result Summaries ---
data class ChatMessage(val sender: String, val text: String)
data class TestResult(
    val title: String,
    val totalScore: Int,
    val score: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val percentage: Int,
    val details: List<QuestionResultDetail>
)
data class QuestionResultDetail(
    val questionText: String,
    val chosenOptionIndex: Int, // -1 if unattempted
    val correctOptionIndex: Int,
    val explanation: String
)
