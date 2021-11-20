package com.example.eventsmanager.data

import androidx.lifecycle.LiveData

class EventRepository(private val eventDao: EventDao) {

    val readAllData : LiveData<List<Event>> = eventDao.readAllData()

    suspend fun addEvent(event: Event){
        eventDao.addEvent(event)
    }

    suspend fun updateEvent(event: Event){
        eventDao.updateEvent(event)
    }
}