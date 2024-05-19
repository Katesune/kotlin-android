package com.bignerdranch.android.geomain

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel() : ViewModel() {

    var currentIndex = 0
    var isCheater = false
    private val availableCheats = 3

    private val questionBlank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true))

    private val answers = mutableMapOf<Int, Boolean>()

    private val questionsCheat = (questionBlank.indices)
        .map{false}
        .toMutableList()

    val currentQuestionAnswer: Boolean
        get() = questionBlank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBlank[currentIndex].textResId

    val userAnswerExists: Boolean
        get() = answers.containsKey(currentIndex)

    val answersFilled: Boolean
     get() = answers.size == questionBlank.size

    val availableCheatsCount: Int
        get() = availableCheats - questionsCheat.count{it}

    val cheatIsAvailable: Boolean
        get() = questionsCheat.count{it} < availableCheats

    val currentQuestionCheat: Boolean
        get() = questionsCheat[currentIndex]

    val score: Float
        get() {
            val rightAnswers = answers.count{it.value}.toFloat()
            val answersCount = answers.size.toFloat()
            return (rightAnswers / answersCount) * 100
        }

    fun moveToPrev() {
        currentIndex = if (currentIndex == 0) questionBlank.size - 1
        else kotlin.math.abs((currentIndex - 1) % questionBlank.size)
    }

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBlank.size
    }

    fun fixUserResult(userAnswer: Boolean) {
        val result = (userAnswer == currentQuestionAnswer)
        answers += currentIndex to result
    }

    fun useCheatForCurrentQuestion(cheat: Boolean) {
        questionsCheat[currentIndex] = cheat
    }
}