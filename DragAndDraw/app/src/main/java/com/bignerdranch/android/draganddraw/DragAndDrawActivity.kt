package com.bignerdranch.android.draganddraw

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DragAndDrawActivity : AppCompatActivity() {

    private lateinit var sceneView: View
    private lateinit var sunView: View
    private lateinit var skyView:View

    private val blueSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.blue_sky)
    }

    private val sunsetSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.sunset_sky)
    }

    private val nightSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.night_sky)
    }

    private var sunsetReverse = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.scene)
        sunView = findViewById(R.id.sun)
        skyView = findViewById(R.id.sky)

        sceneView.setOnClickListener {
            val currentAnimator = if (sunsetReverse) ReverseSunsetAnimator() else SunsetAnimator()
            currentAnimator.startAnimation()
            sunsetReverse = !sunsetReverse
        }
    }

    abstract inner class Animation {
        val sunYStart = sunView.y
        val sunYEnd = skyView.bottom.toFloat() + 100

        abstract val yAnimator: ObjectAnimator
        val sunsetAnimator = backGroundColorAnimator(sunsetSkyColor, 3000)
        open val nightSkyAnimator = backGroundColorAnimator(nightSkyColor, 1500)

        open val quakeAnimatorX = quakeAnimator("scaleX")
        open val quakeAnimatorY = quakeAnimator("scaleY")

        val animatorSet = AnimatorSet()

        open fun startAnimation() {
            animatorSet.play(yAnimator)
                    .with(sunsetAnimator)
                    .before(nightSkyAnimator)
                    .with(quakeAnimatorX)
                    .with(quakeAnimatorY)

            animatorSet.start()
        }
    }

    private inner class SunsetAnimator: Animation() {
        override val yAnimator = yAnimator(sunYStart, sunYEnd)
        override val nightSkyAnimator = backGroundColorAnimator(nightSkyColor, 1500)
    }


    private inner class ReverseSunsetAnimator: Animation() {
        val canvasTop = (sceneView.height / 6).toFloat()

        override val yAnimator = yAnimator(sunYStart, canvasTop)
        override val nightSkyAnimator = backGroundColorAnimator(blueSkyColor, 1000)

        override fun startAnimation() {
            super.animatorSet.play(yAnimator)
                    .with(sunsetAnimator)
                    .before(nightSkyAnimator)
                    .with(quakeAnimatorX)
                    .with(quakeAnimatorY)

            animatorSet.start()
        }
    }

    val yAnimator: (Float, Float) -> ObjectAnimator = { yStart, yEnd ->
        val animator = ObjectAnimator
                .ofFloat(sunView, "y", yStart, yEnd)
                .setDuration(3000)

        animator.interpolator = AccelerateInterpolator()
        animator
    }

    val backGroundColorAnimator: (Int, Long) -> ObjectAnimator = { colorEnd, duration ->
        val backgroundColor = (skyView.background as? ColorDrawable)?.color ?: 0
        skyView.setBackgroundColor(colorEnd)

        val animator = ObjectAnimator
                .ofInt(skyView, "backgroundColor", backgroundColor, colorEnd)
                .setDuration(duration)

        animator.setEvaluator(ArgbEvaluator())
        animator
    }

    val quakeAnimator: (String) -> ObjectAnimator = { propertyName ->
        val quakeAnimator = ObjectAnimator
                .ofFloat(sunView, propertyName, 1F, 1.03F)
                .setDuration(200)

        quakeAnimator.repeatCount = 30
        quakeAnimator.interpolator = BounceInterpolator()
        quakeAnimator
    }
}