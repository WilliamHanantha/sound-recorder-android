package com.example.soundrecorder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper (context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "RecordingsDB"
        private const val TABLE_NAME = "recordings"

        private const val KEY_ID = "id"
        private const val KEY_FILE_PATH = "file_path"
        private const val KEY_DATE = "date"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_FILE_PATH TEXT,"
                + "$KEY_DATE TEXT)")

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addRecording(filePath: String, date: String) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(KEY_FILE_PATH, filePath)
        values.put(KEY_DATE, date)

        val newRowId = db.insert(TABLE_NAME, null, values)
        if (newRowId != -1L) {
            Log.d("DB_INSERT", "New recording inserted successfully at row ID: $newRowId")
        } else {
            Log.e("DB_INSERT", "Failed to insert recording!")
        }
        db.close()
    }

    fun getAllRecordings(): List<RecordingModel> {
        val recordingsList = mutableListOf<RecordingModel>()
        val selectQuery = "SELECT * FROM $TABLE_NAME"

        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val filePath = cursor.getString(cursor.getColumnIndex(KEY_FILE_PATH))
                val date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                // Retrieve other attributes as required

                val recording = RecordingModel(filePath, date)
                recordingsList.add(recording)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return recordingsList
    }
}