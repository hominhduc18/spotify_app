package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.Respond.SongRepository
import com.example.myapplication.Respond.SongViewModel
import com.example.myapplication.Respond.SongViewModelFactory
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.adapter.onSongItemClicked
import com.example.myapplication.databinding.FragmentSongListBinding


abstract class SongListFragment : Fragment(),
    onSongItemClicked,
    MediaPlayerListener {

    private lateinit var binding: FragmentSongListBinding
    private lateinit var viewModel: SongViewModel
    private val REQUEST_PERMISSION_CODE = 123
    private lateinit var adapter: SongAdapter
    private var currentSongPosition: Int = -1
    private lateinit var musicPlayerManager: MusicPlayerManager
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var maxVolume: Int = 0
    private var currentVolumeLevel: Int = 0
    private lateinit var playPauseEx: ImageView
    private lateinit var nextEx: ImageView
    private lateinit var previousEx: ImageView
    private lateinit var playPauseCom: ImageView
    private lateinit var nextCom: ImageView
    private lateinit var previousCom: ImageView
    private lateinit var songseekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var progressForwadTv: TextView
    private lateinit var songRemainingTv: TextView
    private lateinit var songTitle: TextView
    private lateinit var songTitleCom: TextView
    private lateinit var songArtistName: TextView
    private lateinit var albumCoverImageView: ImageView

    private lateinit var slideDownAnimation: Animation
    private lateinit var slideUpAnimation: Animation


    private var durationMillis: Long? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_song_list, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initAnimations()

    }

    private fun initAnimations() {
        slideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideUpAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUI() {
        musicPlayerManager = (requireActivity().application as MyAplication).musicPlayerManager

        musicPlayerManager.setMediaPlayerListener(this)


        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        playPauseEx = binding.expandedPlayerLayout.playPauseButton
        nextEx = binding.expandedPlayerLayout.nextButton
        previousEx = binding.expandedPlayerLayout.previousButton

        playPauseCom = binding.compactPlayerLayout.playPauseButtonMinimized
        nextCom = binding.compactPlayerLayout.nextButtonMinimized
        previousCom = binding.compactPlayerLayout.previousButtonMinimized

        songseekBar = binding.expandedPlayerLayout.linearProgressBar
        volumeSeekBar = binding.expandedPlayerLayout.volumeBar

        progressForwadTv = binding.expandedPlayerLayout.startTextView
        songRemainingTv = binding.expandedPlayerLayout.endTextView

        songTitle = binding.expandedPlayerLayout.songTitleTextView
        songTitleCom = binding.compactPlayerLayout.songTitleTextViewMinimized
        songArtistName  =  binding.expandedPlayerLayout.songArtistTextView

        albumCoverImageView  = binding.expandedPlayerLayout.albumCoverImageView





        requestPermissions()

        adapter = SongAdapter()
        adapter.setClickListener(this)

        val repository = SongRepository()
        viewModel = ViewModelProvider(this, SongViewModelFactory(repository)).get(SongViewModel::class.java)

        val upBtn = binding.compactPlayerLayout.upBtn
        val backButton = binding.expandedPlayerLayout.downCollapase




        songseekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayerManager.seekTo(progress.toLong())
                    updateStartTimeTextView()
                    updateEndTimeTextView()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}


        })

        startUpdatingSeekBarProgress()


        playPauseEx.setOnClickListener {
            handlePlayPauseClick(playPauseEx)
        }

        playPauseCom.setOnClickListener {
            handlePlayPauseClick(playPauseCom)
        }

        previousEx.setOnClickListener {
            handlePreviousClick()
        }

        previousCom.setOnClickListener {
            handlePreviousClick()
        }

        nextEx.setOnClickListener {
            handleNextClick()
        }

        nextCom.setOnClickListener {
            handleNextClick()
        }

        volumeSeekBar.max = maxVolume
        currentVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = currentVolumeLevel

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolumeLevel = progress
                setVolume(currentVolumeLevel)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }



    @SuppressLint("ObsoleteSdkInt")
    private fun requestPermissions() {

        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_PERMISSION_CODE)
        } else {
            // Permissions are granted prior to API level 23
            // Proceed with your code
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.showList(requireContext()).observe(viewLifecycleOwner, Observer {
                    adapter.setList(it!!)
                    binding.rvSongList.adapter = adapter
                })
            } else {
                // Permission denied, handle accordingly (e.g., show a message or take appropriate action)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocus(): Boolean {
        return audioFocusRequest?.let { request ->
            val focusRequest = request
            // Additional logic here if needed
            true // Return a Boolean value indicating success
        } ?: run {
            val requestBuilder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).apply {
                setAudioAttributes(AudioAttributes.Builder().apply {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                }.build())
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(audioFocusChangeListener, handler)
            }
            audioFocusRequest = requestBuilder.build()

            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun releaseAudioFocus() {
        audioFocusRequest?.let { request ->
            audioManager.abandonAudioFocusRequest(request)
            audioFocusRequest = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (musicPlayerManager.isPlaying()) {
                    musicPlayerManager.pauseSong(playPauseEx)
                    playPauseEx.setImageResource(R.drawable.play)
                    releaseAudioFocus()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (musicPlayerManager.isPlaying()) {
                    musicPlayerManager.pauseSong(playPauseEx)
                    playPauseEx.setImageResource(R.drawable.play)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!musicPlayerManager.isPlaying()) {
                    musicPlayerManager.resumeSong(playPauseEx)
                    playPauseEx.setImageResource(R.drawable.pause)
                }
            }
        }
    }

    fun updateSeekBarProgress() {
        while (true) {
            try {
                if (musicPlayerManager.isPlaying()) {
                    val message = Message()
                    message.what = musicPlayerManager.getCurrentPosition().toInt()
                    handler.sendMessage(message)
                    Thread.sleep(200)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            songseekBar.progress = msg.what
            updateStartTimeTextView()
            updateEndTimeTextView()
        }
    }

    fun startUpdatingSeekBarProgress() {
        Thread {
            updateSeekBarProgress()
        }.start()
    }

    fun updateStartTimeTextView() {
        val currentTime = musicPlayerManager.getCurrentPosition()
        val startTime = formatTime(currentTime)
        progressForwadTv.text = startTime
    }

    fun updateEndTimeTextView() {
        val duration = musicPlayerManager.getDuration()
        val currentTime = musicPlayerManager.getCurrentPosition()
        val endTime = formatTime(duration - currentTime)
        songRemainingTv.text = "-$endTime"
    }

    private fun formatTime(millis: Long): String {
        val minutes = millis / (1000 * 60)
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun setVolume(volumeLevel: Int) {
        val volume = volumeLevel / 100f
        musicPlayerManager.setVolume(volume, volume)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePlayPauseClick(button: ImageView) {




        if (musicPlayerManager.isPlaying()) {
            musicPlayerManager.pauseSong(button)
            button.setImageResource(R.drawable.play)
            releaseAudioFocus()
        } else {
            if (requestAudioFocus()) {
                musicPlayerManager.resumeSong(button)
                button.setImageResource(R.drawable.pause)
            }
        }


        updateUI()



    }

    private fun handlePreviousClick() {
        if (currentSongPosition > 0) {
            currentSongPosition--
        } else {
            currentSongPosition = adapter.itemCount - 1
        }
        val previousSong = adapter.getItem(currentSongPosition)
        musicPlayerManager.playSong(previousSong, playPauseEx, songTitle, songArtistName, albumCoverImageView)
        updateCompactPlayerUI()



    }

    private fun handleNextClick() {
        if (currentSongPosition < (adapter.itemCount - 1)) {
            currentSongPosition++
        } else {
            currentSongPosition = 0
        }
        val nextSong = adapter.getItem(currentSongPosition)
        musicPlayerManager.playSong(nextSong, playPauseEx, songTitle, songArtistName, albumCoverImageView)
        updateCompactPlayerUI()

    }


    private fun updateCompactPlayerUI() {
        updateCompactPlayerPlayPauseButton()
        updateCompactPlayerSongTitle()
    }


    fun updateUI() {
        updateStartTimeTextView()
        updateEndTimeTextView()
        updatePlayPauseButton()

        updateCompactPlayerPlayPauseButton()
        updateCompactPlayerSongTitle()
    }

    fun updatePlayPauseButton() {
        val isPlaying = musicPlayerManager.isPlaying()
    }

    private fun updateCompactPlayerPlayPauseButton() {
        val isPlaying = musicPlayerManager.isPlaying()
        playPauseCom.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play_white)
    }

    private fun updateCompactPlayerSongTitle() {
        val currentSong = adapter.getItem(currentSongPosition)
        songTitleCom.text = currentSong.title

        val textWidth = songTitleCom.paint.measureText(songTitleCom.text.toString())
        val screenWidth = requireContext().resources.displayMetrics.widthPixels.toFloat()

        val translateAnimation = TranslateAnimation(screenWidth, -textWidth, 0f, 0f)
        translateAnimation.duration = (textWidth / screenWidth * 10000).toLong() // Adjust the duration as per your preference
        translateAnimation.repeatCount = Animation.INFINITE
        translateAnimation.interpolator = LinearInterpolator()

        songTitleCom.startAnimation(translateAnimation)

    }





}


