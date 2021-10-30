package com.example.eventsmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventsmanager.data.Event
import com.example.eventsmanager.databinding.RvLayoutBinding

class EventAdapter: RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    lateinit var binding: RvLayoutBinding

    private var eventList = emptyList<Event>()
    private var activate: Boolean = false
    private var editType: Int = 0;

    class ViewHolder(private val binding: RvLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

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

        if (activate){
            binding.editBtn.visibility = View.VISIBLE
            if(editType == 0){
                binding.editBtn.setImageResource(R.drawable.ic_delete)
            }
            else{
                binding.editBtn.setImageResource(R.drawable.ic_update)
            }
        } else{
            binding.editBtn.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setData(events : List<Event>){
        this.eventList = events
        notifyDataSetChanged()
    }

    fun setActivate(activate: Boolean){
        this.activate = activate
        notifyDataSetChanged()
    }
    fun setActivate(activate: Boolean, type: Int){
        this.activate = activate
        editType = type
        notifyDataSetChanged()
    }
}