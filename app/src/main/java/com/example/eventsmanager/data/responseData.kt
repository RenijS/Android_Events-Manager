package com.example.eventsmanager.data

data class responseData(
    var shiftId: Int? = null,

    var startTime: String? = null,

    var endTime: String? = null,

    var storeId: Int? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    var detail: String? = null,

    val title: String? = ""
)