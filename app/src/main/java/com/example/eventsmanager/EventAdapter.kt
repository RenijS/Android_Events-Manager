package com.example.eventsmanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventsmanager.data.Event
import com.example.eventsmanager.databinding.AddEventsLayoutBinding
import com.example.eventsmanager.databinding.RvLayoutBinding

class EventAdapter(private val context: Context, private val listener: OnItemClickListener): RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    lateinit var binding: RvLayoutBinding

    private var eventList = emptyList<Event>()
    private var activate: Boolean = false

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(binding: RvLayoutBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        binding = RvLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventAdapter.ViewHolder, position: Int) {
        val currentEvent = eventList[position]
        binding.tvTitle.text = currentEvent.title
        binding.tvStartTime.text = currentEvent.startTime
        binding.tvEndTime.text = currentEvent.endTime
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setData(events : List<Event>){
        this.eventList = emptyList()
        this.eventList = events
        notifyDataSetChanged()
    }


    fun setActivate(activate: Boolean){
        this.activate = activate
        notifyDataSetChanged()
    }

}