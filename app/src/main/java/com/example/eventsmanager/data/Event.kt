package com.example.eventsmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val eventId: Int,

    val startTime: String,

    val endTime: String?,

    val latitude: Double? = null,

    val longitude: Double? = null,

    var detail: String? = null,

    val title: String
)