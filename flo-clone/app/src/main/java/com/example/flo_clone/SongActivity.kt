package com.example.flo_clone

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.flo_clone.databinding.ActivitySongBinding
import com.google.gson.Gson

class SongActivity : AppCompatActivity() {
    lateinit var binding: ActivitySongBinding
    lateinit var song: Song
    lateinit var timer: Timer
    private var mediaPlayer: MediaPlayer? = null
    private var gson: Gson = Gson()

    val songs = arrayListOf<Song>()
    lateinit var songDB: SongDatabase
    var nowPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayList()
        initSong()

        setPlayer(song)

        binding.songRepeatIv.setOnClickListener{

            timer.interrupt()
            startTimer()

        }

        binding.songDownIb.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("title", binding.songMusicTitleTv.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.songMiniplayerIv.setOnClickListener {
            setPlayerStatus(true)
        }

        binding.songPauseIv.setOnClickListener {
            setPlayerStatus(false)
        }
    }

    override fun onPause(){
        super.onPause()
        setPlayerStatus(false)
        song.second = ((binding.songProgressSb.progress * song.playTime)/100)/1000
        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit() // 에디터
        val songJson = gson.toJson(song)
        editor.putString("songData",songJson)
        editor.apply()

    }

    override fun onDestroy() {
        super.onDestroy()
        timer.interrupt()
        mediaPlayer?.release() //리소스 해제
        mediaPlayer = null //
    }

    private fun initPlayList(){
        songDB = SongDatabase.getInstance(this)!!
        songs.addAll(songDB.songDao().getSongs())
    }

    private fun initSong(){
        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId",0)

        nowPos = getPlayingSongPosition(songId)

        Log.d("now Song ID",songs[nowPos].id.toString())

        startTimer()
        setPlayer(songs[nowPos])
    }

    private fun setPlayer(song: Song) {
        binding.songMusicTitleTv.text = song.title
        binding.songSingerNameTv.text = song.singer
        binding.songStartTimeTv.text =
            String.format("%02d:%02d", song.second / 60, song.second % 60)
        binding.songEndTimeTv.text =
            String.format("%02d:%02d", song.playTime / 60, song.playTime % 60)
        binding.songProgressSb.progress = (song.second * 1000) / song.playTime
        val music = resources.getIdentifier(song.music, "raw", this.packageName)
        mediaPlayer = MediaPlayer.create(this,music)
        setPlayerStatus(song.isPlaying)
    }

    private fun setPlayerStatus(isPlaying: Boolean) {
        song.isPlaying = isPlaying
        timer.isPlaying = isPlaying

        if (isPlaying) {
            binding.songMiniplayerIv.visibility = View.GONE
            binding.songPauseIv.visibility = View.VISIBLE
            mediaPlayer?.start()
        } else {
            binding.songMiniplayerIv.visibility = View.VISIBLE
            binding.songPauseIv.visibility = View.GONE
            if(mediaPlayer?.isPlaying == true){
                mediaPlayer?.pause()
            }
        }
    }

    private fun startTimer() {
        timer = Timer(song.playTime, song.isPlaying)
        timer.start()
    }

    inner class Timer(private val playTime: Int, var isPlaying: Boolean = true): Thread() {
        private var second : Int = 0
        private var mills : Float = 0f

        override fun run() {
            try {
                while (true) {
                    if (isPlaying) {
                        sleep(50)
                        mills += 50
                        runOnUiThread {
                            binding.songProgressSb.progress = ((mills / playTime) * 100).toInt()
                        }

                        if (mills % 1000 == 0f) {
                            runOnUiThread {
                                binding.songStartTimeTv.text =
                                    String.format("%02d:%02d", second / 60, second % 60)
                            }
                            second++
                        }
                    }
                }
            } catch (e: InterruptedException) {
                Log.d("Song", "스레드가 죽었습니다. ${e.message}")
            }
        }
    }

    private fun getPlayingSongPosition(songId: Int): Int{
        for (i in 0 until songs.size){
            if (songs[i].id == songId){
                return i
            }
        }
        return 0
    }
}

