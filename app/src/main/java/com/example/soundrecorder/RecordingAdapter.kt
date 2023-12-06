package com.example.soundrecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordingAdapter(private val recordings: List<RecordingModel>) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recording = recordings[position]
        holder.bind(recording)
    }

    override fun getItemCount(): Int {
        return recordings.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val filePathTextView: TextView = itemView.findViewById(R.id.textViewFilePath)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val buttonPlay: LinearLayout = itemView.findViewById(R.id.buttonPlay)

        fun bind(recording: RecordingModel) {
            val number = position + 1
            filePathTextView.text = recording.filePath.replace("/storage/emulated/0/Download/", "$number. ").replace(".mp3" , "")

            dateTextView.text = recording.date
            // Bind other recording info to corresponding views
        }
        init {
            buttonPlay.setOnClickListener{
                val position = adapterPosition
                val recordingModel = recordings[position]
                val path = recordingModel.filePath
                onPlayClickListener.onPlayClick(path)
            }
        }
    }

    interface OnPlayClickListener {
        fun onPlayClick(path: String)
    }

    private lateinit var onPlayClickListener: OnPlayClickListener

    fun setOnPlayCLickListener(listener: OnPlayClickListener) {
        this.onPlayClickListener = listener
    }
}
