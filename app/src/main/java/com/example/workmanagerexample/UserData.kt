package com.example.workmanagerexample

import java.time.Instant

data class UserData(
    val cont : Int,
    val timeStamp : String,
//    val deviceId: String,
//    val paymentId: String
    val statusInt : Int
)

enum class StatusRequest{
    INIT,
    FINISHED,
    CANCELLED,
    RUNNING
}

fun mapStatusColor(response: Int): Int {
    return when (response) {
        StatusRequest.INIT.ordinal -> R.color.gray
        StatusRequest.RUNNING.ordinal -> R.color.green
        StatusRequest.CANCELLED.ordinal -> R.color.red
        StatusRequest.FINISHED.ordinal -> R.color.purple_500
        else -> R.color.black
    }
}

fun mapStatusText(response : Int): String{
    return when(response){
        StatusRequest.INIT.ordinal -> "init"
        StatusRequest.CANCELLED.ordinal -> "Cancelled"
        StatusRequest.RUNNING.ordinal -> "Running"
        StatusRequest.FINISHED.ordinal -> "Finished"
        else -> "Unknown"
    }
}