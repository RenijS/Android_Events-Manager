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
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mEventViewModel: EventViewModel

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: AddEventsLayoutBinding

    private lateinit var addBtn: FloatingActionButton
    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormatter = SimpleDateFormat("dd/MMM/YYYY", Locale.US)

    val timeFormatter = SimpleDateFormat("HH/mm", Locale.US)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        initWidget()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initWidget(){
        addBtn = binding.addBtn

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
                    dialogBinding.tvStart.setHintTextColor(Color.RED)
                    dialogBinding.tvStart.hint = "Please select time and date"
                    dialogBinding.tvEnd.setHintTextColor(Color.RED)
                    dialogBinding.tvEnd.hint = "Please select time and date"
                }
                else if (dialogBinding.etTitle.text.isEmpty()){
                    Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show()
                    dialogBinding.etTitle.setHintTextColor(Color.RED)
                    dialogBinding.etTitle.hint = "Please enter title"
                }
                else{
                    //insertDataToDatabase()
                }
            }
            dialogBinding.llStart.setOnClickListener {
                val calendar: Calendar = Calendar.getInstance()
                showDatePickerDialog(calendar)
                showTimePickerDialog(calendar)
            }
            dialog.setCancelable(false)
            dialog.show()
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private fun showDatePickerDialog(calendar: Calendar){
        val datePickerDialog =
            DatePickerDialog(this@MainActivity, DatePickerDialog.OnDateSetListener { datePicker, yr, mnth, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(yr, mnth, dayOfMonth)
                val date = dateFormatter.format(selectedDate.time)
                Toast.makeText(this, "Date: $date", Toast.LENGTH_LONG).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).visibility = View.GONE
    }

    private fun showTimePickerDialog(calendar: Calendar){
        val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, min ->
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, min)
            val time = timeFormatter.format(selectedTime.time)
            Toast.makeText(this, "Time: $time", Toast.LENGTH_LONG).show()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.setCancelable(false)
        timePickerDialog.show()
        timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).visibility = View.GONE
    }

    private fun insertDataToDatabase() {
        val title = dialogBinding.etTitle.text
        val location = dialogBinding.tvLocation.text
        val desc = dialogBinding.tvDes.text

        val date = null
    }
}