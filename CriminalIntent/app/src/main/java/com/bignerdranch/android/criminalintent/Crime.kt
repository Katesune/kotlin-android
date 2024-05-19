package com.bignerdranch.android.criminalintent

import androidx.annotation.LayoutRes
import java.util.*

data class Crime(val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    @LayoutRes var requiredPolice: Int  = R.layout.list_item_crime)