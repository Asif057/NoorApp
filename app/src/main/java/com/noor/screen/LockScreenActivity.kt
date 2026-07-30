package com.noor.screen

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class LockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lockscreen Overlay flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        enableEdgeToEdge()

        setContent {
            NoorTheme {
                LockScreenUI(
                    onQuizPassed = {
                        TimerManager.resetTimer(this)
                        TimerManager.isTimeUp = false
                        Toast.makeText(this, "Masha'Allah! App unlocked.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

// Deep Navy & Gold Colors
val DeepNavy = Color(0xFF0F172A)
val SurfaceNavy = Color(0xFF1E293B)
val GoldAccent = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF3E5AB)
val TextOffWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val ErrorRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)

@Composable
fun NoorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = DeepNavy,
            surface = SurfaceNavy,
            primary = GoldAccent,
            onBackground = TextOffWhite,
            onSurface = TextOffWhite,
            onPrimary = DeepNavy
        ),
        content = content
    )
}

enum class LockPhase {
    READING,
    QUIZ,
    SUCCESS
}

data class QuizQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun LockScreenUI(onQuizPassed: () -> Unit) {
    // Intercept back button so user cannot exit without passing
    BackHandler {
        // Do nothing to keep app locked
    }

    var phase by remember { mutableStateOf(LockPhase.READING) }
    val ayats by remember { mutableStateOf(QuranData.getSequentialAyats(5)) }

    // Quiz Questions State
    val questions by remember(ayats) {
        mutableStateOf(generateQuestionsForAyats(ayats))
    }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Banner
            LockScreenHeader(phase = phase)

            Spacer(modifier = Modifier.height(16.dp))

            when (phase) {
                LockPhase.READING -> {
                    ReadingPhaseView(
                        ayats = ayats,
                        onProceedToQuiz = { phase = LockPhase.QUIZ }
                    )
                }
                LockPhase.QUIZ -> {
                    if (currentQuestionIndex < questions.size) {
                        QuizPhaseView(
                            questionNumber = currentQuestionIndex + 1,
                            totalQuestions = questions.size,
                            question = questions[currentQuestionIndex],
                            errorMessage = errorMessage,
                            onOptionSelected = { selectedIndex ->
                                if (selectedIndex == questions[currentQuestionIndex].correctIndex) {
                                    errorMessage = null
                                    if (currentQuestionIndex + 1 < questions.size) {
                                        currentQuestionIndex++
                                    } else {
                                        phase = LockPhase.SUCCESS
                                    }
                                } else {
                                    errorMessage = "Try again! That was not the correct answer."
                                }
                            }
                        )
                    }
                }
                LockPhase.SUCCESS -> {
                    SuccessPhaseView(onUnlock = onQuizPassed)
                }
            }
        }
    }
}

@Composable
fun LockScreenHeader(phase: LockPhase) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = GoldAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NOOR SCREEN LOCK",
                style = MaterialTheme.typography.labelLarge,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when (phase) {
                LockPhase.READING -> "Read 5 Sequential Ayats to Unlock"
                LockPhase.QUIZ -> "Short Knowledge Check"
                LockPhase.SUCCESS -> "Unlock Granted"
            },
            style = MaterialTheme.typography.titleMedium,
            color = TextOffWhite,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ReadingPhaseView(
    ayats: List<Ayat>,
    onProceedToQuiz: () -> Unit
) {
    val scrollState = rememberScrollState()

    val surahName = ayats.firstOrNull()?.surahName ?: "Surah"
    val juzNumber = ayats.firstOrNull()?.juzNumber ?: 1

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Surah & Juz Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$surahName  •  Juz $juzNumber",
                    color = GoldLight,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Quran Verses Scroll Container
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ayats.forEachIndexed { index, ayat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        // Ayat Badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f))
                                .border(1.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${ayat.ayatNumber}",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Arabic Text
                        Text(
                            text = ayat.arabicText,
                            color = TextOffWhite,
                            fontSize = 28.sp,
                            lineHeight = 48.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (ayat.translation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ayat.translation,
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (index < ayats.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(1.dp)
                                    .background(GoldAccent.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Button(
            onClick = onProceedToQuiz,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent,
                contentColor = DeepNavy
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I HAVE READ. TAKE THE QUIZ",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun QuizPhaseView(
    questionNumber: Int,
    totalQuestions: Int,
    question: QuizQuestion,
    errorMessage: String?,
    onOptionSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question $questionNumber of $totalQuestions",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${(questionNumber * 100) / totalQuestions}%",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { questionNumber.toFloat() / totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldAccent,
                trackColor = SurfaceNavy
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = question.prompt,
                    color = TextOffWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error Feedback if wrong answer clicked
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = TextOffWhite,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Options List
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            question.options.forEachIndexed { index, optionText ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .clickable { onOptionSelected(index) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = listOf("A", "B", "C", "D").getOrElse(index) { "${index + 1}" },
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            color = TextOffWhite,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessPhaseView(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.3f), SurfaceNavy)
                    )
                )
                .border(2.dp, GoldAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = GoldAccent,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Masha'Allah!",
            style = MaterialTheme.typography.headlineMedium,
            color = GoldLight,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You passed the Quran quiz. Your screen-time timer has been reset and access is restored.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextOffWhite,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onUnlock,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent,
                contentColor = DeepNavy
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "CONTINUE TO APP",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

private fun generateQuestionsForAyats(ayats: List<Ayat>): List<QuizQuestion> {
    val questions = mutableListOf<QuizQuestion>()

    val currentSurah = ayats.firstOrNull()?.surahName ?: "Surah Al-Fatihah"
    val currentJuz = ayats.firstOrNull()?.juzNumber ?: 1

    // Question 1: Surah Name
    val allSurahs = listOf("Surah Al-Fatihah", "Surah Al-Baqarah", "Surah Al-Imran", "Surah An-Nisa", "Surah Al-Kahf", "Surah Ya-Sin")
    val surahDistractors = allSurahs.filter { it != currentSurah }.shuffled().take(3)
    val surahOptions = (surahDistractors + currentSurah).shuffled()
    val correctSurahIndex = surahOptions.indexOf(currentSurah)

    questions.add(
        QuizQuestion(
            prompt = "What is the name of the Surah for the Ayats you just read?",
            options = surahOptions,
            correctIndex = correctSurahIndex
        )
    )

    // Question 2: Juz Number
    val currentJuzStr = "Juz $currentJuz"
    val juzDistractors = listOf(1, 2, 3, 15, 29, 30)
        .filter { it != currentJuz }
        .shuffled()
        .take(3)
        .map { "Juz $it" }
    val juzOptions = (juzDistractors + currentJuzStr).shuffled()
    val correctJuzIndex = juzOptions.indexOf(currentJuzStr)

    questions.add(
        QuizQuestion(
            prompt = "Which Para (Juz) are these Ayats located in?",
            options = juzOptions,
            correctIndex = correctJuzIndex
        )
    )

    // Question 3: Word Identification
    // Extract a prominent word from one of the Ayats
    val chosenAyat = ayats.random()
    val words = chosenAyat.arabicText.split(" ")
        .map { it.replace(Regex("[^\\p{L}]"), "") }
        .filter { it.length >= 3 }

    val randomWord = if (words.isNotEmpty()) words.random() else "اللَّهِ"
    val correctAyatNumStr = "Ayat ${chosenAyat.ayatNumber}"

    val otherAyatNums = (1..10)
        .filter { it != chosenAyat.ayatNumber }
        .shuffled()
        .take(3)
        .map { "Ayat $it" }

    val wordOptions = (otherAyatNums + correctAyatNumStr).shuffled()
    val correctWordIndex = wordOptions.indexOf(correctAyatNumStr)

    questions.add(
        QuizQuestion(
            prompt = "Which Ayat contains the word:\n\"$randomWord\"?",
            options = wordOptions,
            correctIndex = correctWordIndex
        )
    )

    return questions
}
