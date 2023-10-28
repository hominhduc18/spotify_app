package com.example.myapplication

import android.media.MediaPlayer
import android.net.Uri
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.model.SONG

class MusicPlayerManager : MediaPlayer.OnCompletionListener {
    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false
    val context = MyAplication.instance
    var songName: String = ""
    private var mediaPlayerListener: MediaPlayerListener? = null




    fun playSong(song: SONG, playPause: ImageView, tv: TextView, artistName: TextView, albumArt: ImageView) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(song.data)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } else {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(song.data)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }
        isPaused = false
        songName = song.title!!
        artistName.text = song.artist!!
        mediaPlayer!!.setOnCompletionListener(this)



        val uri = Uri.parse(song.image)

        albumArt.setImageURI(uri)

        updatePlayPauseButtonUI(playPause)
        animateSongNameScroll(tv)

    }

    fun pauseSong(playPause: ImageView) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
        updatePlayPauseButtonUI(playPause)
    }

    fun resumeSong(playPause: ImageView) {
        if (isPaused) {
            mediaPlayer?.start()
            isPaused = false
        }
        updatePlayPauseButtonUI(playPause)
    }
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }




    fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    private fun updatePlayPauseButtonUI(playPause: ImageView) {
        playPause.setImageResource(if (isPlaying()) R.drawable.pause else R.drawable.play)
    }

    private fun animateSongNameScroll(textView: TextView) {
        textView.text = songName

        val textWidth = textView.paint.measureText(textView.text.toString())
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()

        val translateAnimation = TranslateAnimation(screenWidth, -textWidth, 0f, 0f)
        translateAnimation.duration = (textWidth / screenWidth * 10000).toLong()
        translateAnimation.repeatCount = Animation.INFINITE
        translateAnimation.interpolator = LinearInterpolator()

        textView.startAnimation(translateAnimation)
    }

    fun getCurrentPosition() : Long{

        return mediaPlayer!!.currentPosition.toLong()
    }
    fun seekTo(value: Long){
        mediaPlayer!!.seekTo(value.toInt())
    }
    fun getDuration() : Long{
        return mediaPlayer!!.duration.toLong()
    }

    fun setVolume(vol1: Float, vol2: Float){

        mediaPlayer!!.setVolume(vol1, vol2)

    }
    fun setMediaPlayerListener(listener: MediaPlayerListener) {
        mediaPlayerListener = listener
    }

    override fun onCompletion(p0: MediaPlayer?) {
        mediaPlayerListener?.onSongCompletion()

    }
}
interface MediaPlayerListener {
    fun onSongCompletion()
}
