package com.example.soundrecorder

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.soundrecorder.databinding.ActivityMainBinding
import java.io.IOException
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private var recordingList: List<RecordingModel>? = null
    private var adapter: RecordingAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        dbHelper = DBHelper(this)

        recyclerView = findViewById(R.id.recyclerViewRecordings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recordingList = dbHelper.getAllRecordings()

        // Set the RecyclerView adapter
        adapter = RecordingAdapter(recordingList!!)
        recyclerView.adapter = adapter

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val date = currentDateTime.format(formatter)

        binding.buttonStartRecording.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)
            } else {
                startRecording()
            }
        }
        binding.buttonStopRecording.setOnClickListener{
            stopRecording()
            recordingList = dbHelper.getAllRecordings()

            adapter = RecordingAdapter(recordingList!!)
            recyclerView.adapter = adapter
            adapter!!.setOnPlayCLickListener(object : RecordingAdapter.OnPlayClickListener {
                override fun onPlayClick(path: String) {
                    playRecording(path)
                }
            })
        }

        binding.buttonPauseRecording.setOnClickListener {
            pauseRecording()
        }

        adapter!!.setOnPlayCLickListener(object : RecordingAdapter.OnPlayClickListener {
            override fun onPlayClick(path: String) {
//                if (path != null) {
//                    playRecording(path!!)
//                } else {
//                    Toast.makeText(this@MainActivity, "No recording to play", Toast.LENGTH_SHORT).show()
//                }
                playRecording(path)
            }
        })
    }

    private fun playRecording(path: String){
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MediaPlayer", "Error playing file: ${e.message}")
        }
    }

    @SuppressLint("NewApi")
    private fun startRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.format(Date())

        val timeNow = System.currentTimeMillis()

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_name_recording, null)
        dialogBuilder.setView(dialogView)

        val editTextName = dialogView.findViewById(R.id.editTextName) as EditText

        dialogBuilder.setTitle("Name Your Recording")
        dialogBuilder.setPositiveButton("Set", DialogInterface.OnClickListener { dialog, whichButton ->
            val recordingName = editTextName.text.toString().trim()
            val fileName = if (recordingName.isNotEmpty()) {
                "$recordingName.mp3"
            } else {
                "${System.currentTimeMillis()}-recording.mp3"
            }

            output = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/$fileName"
            mediaRecorder?.setOutputFile(output)
            dbHelper.addRecording(output ?: "", date.toString())
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        })

        dialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, whichButton ->
            // Do nothing, cancel the recording
        })

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }



    private fun stopRecording(){
        if(state){
            try {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                state = false
                Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("error", e.toString())
            }
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if(state) {
            if(!recordingStopped){
                Toast.makeText(this,"Stopped!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                binding.buttonPauseRecording.text = "Resume"
            }else{
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this,"Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        binding.buttonPauseRecording.text = "Pause"
        recordingStopped = false
    }
}