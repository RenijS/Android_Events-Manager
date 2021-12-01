package com.example.eventsmanager

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventsmanager.data.Event
import com.example.eventsmanager.data.EventViewModel
import com.example.eventsmanager.databinding.ActivityMainBinding
import com.example.eventsmanager.databinding.AddEventsLayoutBinding
import com.example.eventsmanager.databinding.RvLayoutBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.AssertionError
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, EventAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    private lateinit var mEventViewModel: EventViewModel

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: AddEventsLayoutBinding

    private lateinit var addBtn: FloatingActionButton
    private var today = Calendar.getInstance()
    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
    private val monthFormatter = SimpleDateFormat("MMM")
    @RequiresApi(Build.VERSION_CODES.O)
    private val eventTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private val adapter = EventAdapter(this, this)

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromEnd: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_end_anim) }
    private val toEnd: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_end_anim) }

    private val READ_CALENDAR_RQ = 101
    private val WRITE_CALENDAR_RQ = 102

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        initWidget()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(position: Int) {
        Toast.makeText(this, "clicked: $position", Toast.LENGTH_SHORT).show()
        val dialog = Dialog(this)
        dialogBinding = AddEventsLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.addBtn.text = "Update"

        var eventId: Int? = null
        var startTime: String? = null
        var endTime: String? = null
        var location: String? = null
        var detail: String? = null
        var title: String? = null

        mEventViewModel.readAllData.observe(this, androidx.lifecycle.Observer { events->
            eventId = events[position].eventId
            startTime = events[position].startTime
            endTime = events[position].endTime
            location = events[position].location
            detail = events[position].detail
            title = events[position].title
        })
        dialogBinding.etTitle.setText(title)
        dialogBinding.etDes.setText(detail)
        dialogBinding.etLocation.setText(location)
        dialogBinding.tvStart.text = startTime
        dialogBinding.tvEnd.text = endTime

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
                mEventViewModel.updateEvent(Event(eventId!!,dialogBinding.tvStart.text.toString(),dialogBinding.tvEnd.text.toString(),dialogBinding.etLocation.text.toString(),dialogBinding.etDes.text.toString(),dialogBinding.etTitle.text.toString()))
                mEventViewModel.readAllData.observe(this, androidx.lifecycle.Observer { events->
                    adapter.setData(events)
                })
                dialog.dismiss()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initWidget(){
        addBtn = binding.addBtn

        val swipeGesture = object : SwipeGesture(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    ItemTouchHelper.LEFT -> {
                        var selectedEvent: Event? = null
                        mEventViewModel.readAllData.observe(this@MainActivity, androidx.lifecycle.Observer { events->
                            //ones deleted adapterPosition becomes -1
                            if(viewHolder.adapterPosition != -1) {
                                selectedEvent = events[viewHolder.adapterPosition]
                            }
                        })
                        mEventViewModel.deleteEvent(selectedEvent!!)
                        mEventViewModel.readAllData.observe(this@MainActivity, androidx.lifecycle.Observer { events->
                            adapter.setData(events)
                        })
                    }
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(binding.rvEvents)

        binding.rvEvents.adapter = adapter
        binding.rvEvents.layoutManager = LinearLayoutManager(this)

        mEventViewModel.readAllData.observe(this, androidx.lifecycle.Observer { events->
            adapter.setData(events)
        })

//        binding.month.text = monthFormatter.format(today.time)
        //for month picker initial month selection
        var selectMonth = today.get(Calendar.MONTH)
        //month picker
//        binding.month.setOnClickListener {
//            val builder = MonthPickerDialog.Builder(this,
//                MonthPickerDialog.OnDateSetListener(){ selectedMonth, _ ->
//                    val cal = Calendar.getInstance()
//                    cal[Calendar.MONTH] = selectedMonth
//                    cal[Calendar.DAY_OF_MONTH] = 1
//                    selectMonth = selectedMonth
//                    binding.month.text = monthFormatter.format(cal.time)
//                    adapter.setFilterList(binding.year.text.toString(), (selectedMonth+1).toString())
//                }
//                , today.get(Calendar.YEAR), today.get(Calendar.MONTH))
//
//            builder.setActivatedMonth(Calendar.MONTH)
//                .setActivatedMonth(selectMonth)
//                .setTitle("Select Month")
//                .showMonthOnly()
//                .build().show()
//        }

        //year picker
//        binding.year.setOnClickListener {
//            val builder = MonthPickerDialog.Builder(this,
//                 MonthPickerDialog.OnDateSetListener() { _, selectedYear ->
//                     binding.year.text = selectedYear.toString()
//                     adapter.setFilterList(selectedYear.toString(), null)
//                 }
//                , today.get(Calendar.YEAR), today.get(Calendar.MONTH))
//
//            builder.setActivatedMonth(Calendar.MONTH)
//                .setMinYear(1999)
//                .setActivatedYear(Integer.parseInt(binding.year.text.toString()))
//                .setMaxYear(today.get(Calendar.YEAR)+25)
//                .setTitle("Select Year")
//                .showYearOnly()
//                .build().show()
//        }

        ArrayAdapter.createFromResource(
            this,
            R.array.selection,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            binding.monthSpinner.adapter = adapter
//            binding.monthSpinner.onItemSelectedListener = this
        }

        //activate for rv button
        var activate = false
        binding.optionsBtn.setOnClickListener {
            buttonVisibilityWork()
            animationWork()
            if (activate){
                activate = false
                adapter.setActivate(activate)
            }
        }

        addBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialogBinding = AddEventsLayoutBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.addBtn.text = "Add"
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
                    insertDataToDatabase()
                    dialog.dismiss()
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

        binding.importBtn.setOnClickListener {
            if (checkForPermission(android.Manifest.permission.READ_CALENDAR, READ_CALENDAR_RQ)
                && checkForPermission(android.Manifest.permission.WRITE_CALENDAR, WRITE_CALENDAR_RQ)
            ) {
                mEventViewModel.readAllData.observe(this, androidx.lifecycle.Observer { events->
                    insertEventsToLocal(events)
                    deleteEvents(events)
                })
            }
        }
    }

    private fun checkForPermission(permission: String, requestCode: Int): Boolean{
        if(ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        requestPermissions(arrayOf(permission), requestCode)
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun innerCheck(name: String){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(this, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }
        when(requestCode){
            READ_CALENDAR_RQ -> innerCheck("read calendar")
            WRITE_CALENDAR_RQ -> innerCheck("write calendar")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertEventsToLocal(eventsList: List<Event>){
        if (eventsList.isEmpty()){
            Toast.makeText(this, "List is empty", Toast.LENGTH_SHORT).show()
        } else {
            eventsList.forEach { event ->
                println("CHeck!: events:  $event")
                if (event.startTime != null) {
                    val startTime = LocalDateTime.parse(event.startTime, eventTimeFormatter)
                    println("Check!!!: entered")
                    if(startTime>=LocalDateTime.now()) {
                        println("Check!!!: entered2")
                        val startMillis: Long = getTimeInLong(event.startTime)
                        val endMillis: Long = getTimeInLong(event.endTime)
                        if (!isEventInCal(event.eventId, 816)) {
                            println("Check!!!: entered3")
                            val values = ContentValues().apply {
                                put(CalendarContract.Events.DTSTART, startMillis)
                                put(CalendarContract.Events.DTEND, endMillis)
                                put(CalendarContract.Events.TITLE, event.title)
                                put(CalendarContract.Events.DESCRIPTION, event.detail)
                                put(CalendarContract.Events.CALENDAR_ID, 2)
                                put(CalendarContract.Events.ORIGINAL_ID, 816)
                                put(CalendarContract.Events.UID_2445, event.eventId)
                                put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Tokyo")
                            }
                            val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                            println("event added: $uri")
                        }
                        else{
                            println("Check!!!: entered4")
                            updateEventInCal(event)
                        }
                    }
                } else {
                    Toast.makeText(this, "Event time length : ${event.startTime.length}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateEventInCal(event: Event) {
        val proj = arrayOf(
            CalendarContract.Instances.ORIGINAL_ID,
            CalendarContract.Instances.UID_2445,
            CalendarContract.Instances._ID
        )
        val selection: String = "((${CalendarContract.Events.ORIGINAL_ID} = ?) AND (" +
                "${CalendarContract.Events.UID_2445} = ?) AND (" +
                "(${CalendarContract.Events.DTSTART} != ?)" +
                " OR (${CalendarContract.Events.DTEND} != ?) OR (${CalendarContract.Events.TITLE} != ?)))"

        val selectionArgs: Array<String> = arrayOf("816", "${event.eventId}"
            , "${getTimeInLong(event.startTime)}", "${getTimeInLong(event.endTime)}", "${event.title}")

        val cursor = contentResolver.query(
            Uri.parse("content://com.android.calendar/events"),
            proj,
            selection,
            selectionArgs,
            null
        )
        try {
            if (cursor!!.count > 0) {
                while (cursor.moveToNext()) {
                    val originalID = cursor.getString(0)
                    val id = cursor.getString(1)
                    val _id = cursor.getString(2)
                    if (originalID != null) {
                        //change
                        val startMillis: Long = getTimeInLong(event.startTime)
                        val endMillis: Long = getTimeInLong(event.endTime)
                        val values = ContentValues().apply {
                            put(CalendarContract.Events.DTSTART, startMillis)
                            put(CalendarContract.Events.DTEND, endMillis)
                            put(CalendarContract.Events.TITLE, event.title)
                        }
                        val updateUri: Uri = ContentUris.withAppendedId(
                            CalendarContract.Events.CONTENT_URI,
                            _id.toLong()
                        )
                        val rows: Int = contentResolver.update(updateUri, values, null, null)
                        println("Rows updated: $rows id: $id _id: $_id")
                    }
                }
            }
        } catch (ex: AssertionError) {
            ex.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteEvents(eventsList: List<Event>) {
        val proj = arrayOf(
            CalendarContract.Instances.UID_2445,
            CalendarContract.Instances._ID
        )
        val cursor = contentResolver.query(
            Uri.parse("content://com.android.calendar/events"),
            proj,
            "${CalendarContract.Events.ORIGINAL_ID} = 816",
            null,
            null
        )

        try {
            if (cursor!!.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val _id = cursor.getString(1)
                    if (id != null) {
                        var found = false
                        for (i in eventsList.indices) {
                            if (!found) {
                                if (eventsList[i].eventId.toString().equals(id)) {
                                    found = true
                                }
                                else if (i == eventsList.size-1) {
                                    //delete the event
                                    val deleteUri: Uri = ContentUris.withAppendedId(
                                        CalendarContract.Events.CONTENT_URI,
                                        _id.toLong()
                                    )
                                    val rows: Int =
                                        contentResolver.delete(deleteUri, null, null)
                                    println("Rows deleted: $rows id: $id _id: $_id")
                                }
                            }
                        }
                    }
                }
            }
        } catch (ex: AssertionError) {
            ex.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeInLong(time: String?): Long{
        val parsedTime = LocalDateTime.parse(time, eventTimeFormatter)
        val timeMillis: Long = Calendar.getInstance().run {
            set(parsedTime.year, parsedTime.monthValue-1, parsedTime.dayOfMonth, parsedTime.hour, parsedTime.minute, parsedTime.second)
            timeInMillis
        }
        return timeMillis
    }

    private fun isEventInCal(id:Int?, identifierId: Int): Boolean{
        val proj = arrayOf(
            CalendarContract.Instances.UID_2445
        )
        val cursor = contentResolver.query(
            Uri.parse("content://com.android.calendar/events"),
            proj,
            "${CalendarContract.Events.UID_2445} = $id AND ${CalendarContract.Events.ORIGINAL_ID} = $identifierId",
            null,
            null
        )
        if(cursor!!.moveToFirst()){
            if (cursor.getString(0).equals(id.toString())) {
                return true
            }
        }
        return false
    }

    private fun buttonVisibilityWork(){
        if(binding.importBtn.visibility == View.VISIBLE){
            binding.importBtn.visibility = View.GONE
            binding.addBtn.visibility = View.GONE

        } else{
            binding.importBtn.visibility = View.VISIBLE
            binding.addBtn.visibility = View.VISIBLE
        }
    }

    private fun animationWork(){
        //use visible instead of gone because we change visibility before animation
        if(binding.importBtn.visibility == View.VISIBLE){
            binding.optionsBtn.startAnimation(rotateOpen)
            binding.addBtn.startAnimation(fromEnd)
            binding.importBtn.startAnimation(fromEnd)
        }
        else{
            binding.optionsBtn.startAnimation(rotateClose)
            binding.addBtn.startAnimation(toEnd)
            binding.importBtn.startAnimation(toEnd)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
        val startTime = dialogBinding.tvStart.text
        val endTime = dialogBinding.tvEnd.text
        val event = Event(0,startTime.toString(),endTime.toString(),location.toString(),desc.toString(),title.toString())
        //add data
        mEventViewModel.addEvent(event)
        Toast.makeText(this, "Added $event", Toast.LENGTH_SHORT).show()
    }


    override fun onItemSelected(p: AdapterView<*>?, view: View?, position: Int, p3: Long) {
        val text = p?.getItemAtPosition(position).toString()
        Toast.makeText(this, "$text selected", Toast.LENGTH_SHORT).show()
//        when(text){
//            "Year" -> {
//                binding.month.visibility = View.GONE
//                binding.year.visibility = View.VISIBLE
//                binding.year.text = today.get(Calendar.YEAR).toString()
//            }
//            "Month+Year" -> {
//                binding.month.visibility = View.VISIBLE
//                binding.year.visibility = View.VISIBLE
//                binding.month.text = monthFormatter.format(today.time).toString()
//            }
//            "All" ->{
//                binding.month.visibility = View.GONE
//                binding.year.visibility = View.GONE
//                adapter.setFilterList(null, null)
//            }
//        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val search = menu?.findItem(R.id.menu_search)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null){
            searchDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null){
            searchDatabase(query)
        }
        return true
    }

    private fun searchDatabase(query: String){
        val searchQuery = "%$query%"
        mEventViewModel.searchDatabase(searchQuery).observe(this, { events ->
            events.let {
                adapter.setData(it)
            }
        })
    }
}