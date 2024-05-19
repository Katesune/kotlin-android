package com.bignerdranch.android.geomain

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var cheatTextView: TextView


    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this, SavedStateViewModelFactory(this.application, this)).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        prevButton = findViewById(R.id.prev_button)
        nextButton = findViewById(R.id.next_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)
        cheatTextView = findViewById(R.id.cheat_counter)

        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
            correctAnswerButtonsEnabled()
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
            correctAnswerButtonsEnabled()
        }

        prevButton.setOnClickListener {
            quizViewModel.moveToPrev()
            updateQuestion()
            correctAllButtonsEnabled()
            displayScore()
        }

        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
            correctAllButtonsEnabled()
            displayScore()
        }

        cheatButton.setOnClickListener { view ->

            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options = ActivityOptions
                    .makeClipRevealAnimation(view, 0, 0, view.width, view.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }

        updateQuestion()
        updateCheatCounter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater =
                data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false

            quizViewModel.useCheatForCurrentQuestion(quizViewModel.isCheater)
            updateCheatCounter()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    private fun updateCheatCounter() {
        val availableCheatsCounter = quizViewModel.availableCheatsCount.toString()
        cheatTextView.text = availableCheatsCounter
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer
        quizViewModel.fixUserResult(userAnswer)

        val messageResId = when {
            quizViewModel.currentQuestionCheat -> R.string.judgement_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
                .show()
    }

    private fun correctAllButtonsEnabled() {
        correctAnswerButtonsEnabled()
        cheatButton.isEnabled = quizViewModel.cheatIsAvailable || quizViewModel.currentQuestionCheat || quizViewModel.answersFilled
    }

    private fun correctAnswerButtonsEnabled() {
        trueButton.isEnabled = !quizViewModel.userAnswerExists
        falseButton.isEnabled = !quizViewModel.userAnswerExists
    }

    private fun displayScore() {
        if (quizViewModel.answersFilled) {
            val score = quizViewModel.score.toInt().toString()

            Toast.makeText(this, score, Toast.LENGTH_SHORT)
                .show()
        }
    }

}