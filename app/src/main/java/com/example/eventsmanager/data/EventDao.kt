package com.example.eventsmanager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addEvent(event: Event)

    @Query("SELECT * FROM event_table ORDER BY eventId ASC")
    fun readAllData(): LiveData<List<Event>>
}