package com.bignerdranch.android.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

private const val TAG = "BoxDrawingView"
private const val SAVE_STATE = "SAVE_BOXEN"
private const val BOXEN = "BOXEN"

class BoxDrawingView(context: Context, attr: AttributeSet? = null):
    View(context, attr) {

    private var currentBox: Box? = null
    private val boxen = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000.toInt()
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)

        boxen.forEach { box ->
            canvas.drawRect(box.left, box.top, box.right, box.bottom, boxPaint)
        }
    }

    private var initialTouchPoint = BoxPoint(0F, 0F)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = BoxPoint(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentBox = Box(current).also {
                    boxen.add(it)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.actionIndex == 0) {
                    updateCurrentBox(current)
                } else {
                    val rotatePoint = initialTouchPoint - current
                    rotateBox(rotatePoint)
                }
            }

            MotionEvent.ACTION_UP -> {
                updateCurrentBox(current)
                currentBox = null
            }

            MotionEvent.ACTION_CANCEL -> {
                currentBox = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                initialTouchPoint = current
            }
        }

        return true
    }

    private fun updateCurrentBox(current: BoxPoint) {
        currentBox?.let {
            it.end = current
            invalidate()
        }
    }

    private fun rotateBox(rotatePoint: BoxPoint) {
        currentBox?.let { box ->
            box.start += rotatePoint
            box.end += rotatePoint
            invalidate()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelableArrayList(BOXEN, boxen.toArrayList())
            putParcelable(SAVE_STATE, super.onSaveInstanceState())
        }
    }

    fun MutableList<Box>.toArrayList(): ArrayList<Box> {
        val boxList = arrayListOf<Box>()
        this.forEach { box ->
            boxList.add(box)
        }
        return boxList
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle

        val savedBoxen = bundle.getParcelableArrayList<Box>(BOXEN)?.toMutableList() ?: mutableListOf()
        boxen.addAll(savedBoxen)
        invalidate()

        super.onRestoreInstanceState(bundle.getParcelable("SAVE_STATE"))
    }
}