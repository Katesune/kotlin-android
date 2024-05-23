package com.bignerdranch.android.criminalintent.database

import android.app.appsearch.SetSchemaResponse
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.criminalintent.Crime
import com.bignerdranch.android.criminalintent.R

@Database(entities = [ Crime::class ], version = 2)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase: RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}

val migration_1_2 = object: Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''",
        )
        database.execSQL(
                "ALTER TABLE Crime ADD COLUMN requiredPolice INT NOT NULL DEFAULT ${R.layout.list_item_crime}",
        )
    }
}