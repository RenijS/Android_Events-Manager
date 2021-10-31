package com.example.eventsmanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventsmanager.data.Event
import com.example.eventsmanager.databinding.AddEventsLayoutBinding
import com.example.eventsmanager.databinding.RvLayoutBinding

class EventAdapter(private val context: Context): RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    lateinit var binding: RvLayoutBinding

    private var eventList = emptyList<Event>()
    private var filterList = emptyList<Event>()
    private var activate: Boolean = false
    private var editType: Int = 0;

    class ViewHolder(private val binding: RvLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        binding = RvLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventAdapter.ViewHolder, position: Int) {
        val currentEvent = filterList[position]
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

        binding.editBtn.setOnClickListener {
            if(editType == 0){
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Delete Event?")
                builder.apply {
                    setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, id ->  })
                    setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, id ->  })
                }
                builder.create()
            }
            else{
                val dialog = Dialog(context)
                val dialogBinding = AddEventsLayoutBinding.inflate(LayoutInflater.from(context))
                dialog.setContentView(dialogBinding.root)
                dialogBinding.addBtn.text = "Save"
                dialogBinding.cancelBtn.setOnClickListener {
                    dialogBinding.addBtn.text = "Add"
                    dialog.dismiss()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setData(events : List<Event>){
        this.eventList = events
        this.filterList = events
        notifyDataSetChanged()
    }

    fun setFilterList(year: String?, month: String?){
        for(event in eventList){
            val startTime = event.startTime
            if(month == null && year == null){
                filterList = eventList
            }
            else if(month == null && year != null) {
                println("check!!!:${startTime.substring(0, startTime.indexOf("-"))}=${year},${startTime.substring(0, startTime.indexOf("-")).equals(year)}")
                if (startTime.substring(0, startTime.indexOf("-")).equals(year)) {
                    filterList = emptyList()

                }
            }
            else{
                if (startTime.substring(0, startTime.indexOf("-")) == year &&
                    startTime.substring(startTime.indexOf("-")+1, startTime.indexOf("-")+3) == month) {
                    println("month year event: $event")
                }
            }
        }
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