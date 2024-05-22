package com.bignerdranch.android.criminalintent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bignerdranch.android.criminalintent.database.CrimeRepository
import kotlin.random.Random

class CrimeListViewModel: ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    private val crimeLiveData = MutableLiveData<List<Crime>>()

    val crimeListLiveData: LiveData<List<Crime>?> =
        Transformations.switchMap(crimeLiveData) {
            crimeRepository.getCrimes()
        }

    fun loadCrimes(crimes: List<Crime>) {
        crimeLiveData.value = crimes
    }

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }
}