package com.bignerdranch.android.criminalintent

import androidx.annotation.LayoutRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = "",
                 @LayoutRes var requiredPolice: Int  = R.layout.list_item_crime
) {
    val photoFileName
        get() = "IMG_$id.jpg"
}
