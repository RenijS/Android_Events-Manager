package com.example.eventsmanager

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.eventsmanager.data.EventViewModel
import com.example.eventsmanager.databinding.ActivityMainBinding
import com.example.eventsmanager.databinding.AddEventsLayoutBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.TimeFormat
import kotlinx.android.synthetic.main.add_events_layout.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mEventViewModel: EventViewModel

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: AddEventsLayoutBinding

    private lateinit var addBtn: FloatingActionButton
    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
    @RequiresApi(Build.VERSION_CODES.O)
    private val eventTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        initWidget()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(binding.optionsBtn.visibility == View.GONE){
            binding.importBtn.visibility = View.GONE
            binding.deleteBtn.visibility = View.GONE
            binding.updateBtn.visibility = View.GONE
            binding.addBtn.visibility = View.GONE
            binding.backBtn.visibility = View.GONE

            binding.optionsBtn.visibility = View.VISIBLE
            binding.llEventsAmount.visibility = View.VISIBLE

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initWidget(){
        addBtn = binding.addBtn

        binding.optionsBtn.setOnClickListener {
            buttonVisibilityWork()
        }
        binding.backBtn.setOnClickListener {
            buttonVisibilityWork()
        }
        addBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialogBinding = AddEventsLayoutBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            dialogBinding.addBtn.setOnClickListener {
                if(dialogBinding.tvStart.text.isEmpty() || dialogBinding.tvEnd.text.isEmpty()){
                    Toast.makeText(this, "Time is empty", Toast.LENGTH_SHORT).show()
                    dialogBinding.tvStart.setHintTextColor(resources.getColor(R.color.red))
                    dialogBinding.tvStart.hint = "Please select time and date"
                    dialogBinding.tvEnd.setHintTextColor(resources.getColor(R.color.red))
                    dialogBinding.tvEnd.hint = "Please select time and date"
                }
                else if (dialogBinding.etTitle.text.isEmpty()){
                    Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show()
                    dialogBinding.etTitle.setHintTextColor(resources.getColor(R.color.red))
                    dialogBinding.etTitle.hint = "Please enter title"
                }
                else if(parseDateTime(dialogBinding.tvStart.text.toString()) > parseDateTime(dialogBinding.tvEnd.text.toString())){
                    Toast.makeText(this, "Start time should come first before end time", Toast.LENGTH_LONG).show()
                }
                else{
                    //insertDataToDatabase()
                }
            }
            dialogBinding.llStart.setOnClickListener {
                val calendar: Calendar = Calendar.getInstance()
                showDatePickerDialog(calendar, 0)
            }
            dialogBinding.llEnd.setOnClickListener {
                val calendar: Calendar = Calendar.getInstance()
                showDatePickerDialog(calendar, 1)
            }
            dialog.setCancelable(false)
            dialog.show()
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun buttonVisibilityWork(){
        if(binding.optionsBtn.visibility == View.GONE){
            binding.importBtn.visibility = View.GONE
            binding.deleteBtn.visibility = View.GONE
            binding.updateBtn.visibility = View.GONE
            binding.addBtn.visibility = View.GONE
            binding.backBtn.visibility = View.GONE

            binding.optionsBtn.visibility = View.VISIBLE
            binding.llEventsAmount.visibility = View.VISIBLE

        } else{
            binding.llEventsAmount.visibility = View.GONE
            binding.optionsBtn.visibility = View.GONE

            binding.importBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.VISIBLE
            binding.updateBtn.visibility = View.VISIBLE
            binding.addBtn.visibility = View.VISIBLE
            binding.backBtn.visibility = View.VISIBLE
        }
    }

    private fun showDatePickerDialog(calendar: Calendar, identifier: Int){
        val datePickerDialog =
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, yr, mnth, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(yr, mnth, dayOfMonth)
                val date = dateFormatter.format(selectedDate.time)
                Toast.makeText(this, "Date: $date", Toast.LENGTH_LONG).show()
                if(identifier == 0) {
                    dialogBinding.tvStart.text = date
                }
                else if (identifier == 1){
                    dialogBinding.tvEnd.text = date
                }
                showTimePickerDialog(calendar, identifier)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).visibility = View.GONE
    }

    private fun showTimePickerDialog(calendar: Calendar, identifier: Int){
        val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, min ->
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, min)
            val time = timeFormatter.format(selectedTime.time)
            Toast.makeText(this, "Time: $time", Toast.LENGTH_LONG).show()
            if(identifier == 0) {
                dialogBinding.tvStart.text = "${dialogBinding.tvStart.text} $time"
            }
            else if(identifier == 1){
                dialogBinding.tvEnd.text = "${dialogBinding.tvEnd.text} $time"
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.setCancelable(false)
        timePickerDialog.show()
        timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDateTime(dateTime: String): LocalDateTime{
        return LocalDateTime.parse(dateTime, eventTimeFormatter)
    }

    private fun insertDataToDatabase() {
        val title = dialogBinding.etTitle.text
        val location = dialogBinding.etLocation.text
        val desc = dialogBinding.etDes.text

        val date = null
    }
}