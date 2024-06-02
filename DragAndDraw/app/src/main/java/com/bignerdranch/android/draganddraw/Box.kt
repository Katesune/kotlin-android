package com.bignerdranch.android.draganddraw

import android.graphics.Canvas
import android.graphics.PointF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Box(var start: BoxPoint) : Parcelable {

    var end: BoxPoint = start

    val left: Float
        get() = Math.min(start.x, end.x)

    val right: Float
        get() = Math.max(start.x, end.x)

    val top: Float
        get() = Math.min(start.y, end.y)

    val bottom: Float
        get() = Math.max(start.y, end.y)
}

class BoxPoint(x: Float, y: Float): PointF(x, y) {
    operator fun minus(point: BoxPoint): BoxPoint = BoxPoint(this.x - point.x, this.y - point.y)

    operator fun plusAssign(point: BoxPoint) {
        this.x += Math.cos(point.x.toDouble()).toFloat()
        this.y += Math.sin(point.y.toDouble()).toFloat()
    }
}