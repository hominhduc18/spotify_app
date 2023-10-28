package com.example.myapplication.Respond

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.entities.Song
import com.example.myapplication.model.SONG

class SongViewModel(
    private val repository: SongRepository
): ViewModel() {


    fun showList(context: Context): LiveData<List<SONG>> {
        return repository.getAudioFile(context)
    }
}