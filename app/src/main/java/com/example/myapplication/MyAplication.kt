package com.example.myapplication

import android.app.Application

class MyAplication: Application() {

    val musicPlayerManager: MusicPlayerManager by lazy { MusicPlayerManager() }


    companion object{
        lateinit var instance : MyAplication
    }

    override fun onCreate(){
        super.onCreate()
        instance = this

    }
}