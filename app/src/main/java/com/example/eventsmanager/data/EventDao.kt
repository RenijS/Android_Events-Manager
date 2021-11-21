package com.example.eventsmanager.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM event_table")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM event_table ORDER BY eventId ASC")
    fun readAllData(): LiveData<List<Event>>
}