package com.example.eventsmanager

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.example.eventsmanager.databinding.ActivityMainBinding
import com.example.eventsmanager.databinding.AddEventsLayoutBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var addBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWidget()
    }

    private fun initWidget(){
        addBtn = binding.addBtn

        addBtn.setOnClickListener {
            val dialog = Dialog(this)
            val dialogBinding = AddEventsLayoutBinding.inflate(layoutInflater)
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
                if (dialogBinding.etTitle.text.isEmpty()){
                    Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show()
                    dialogBinding.etTitle.setHintTextColor(Color.RED)
                    dialogBinding.etTitle.hint = "Please enter title"
                }
            }
            dialog.setCancelable(false)
            dialog.show()
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }
}