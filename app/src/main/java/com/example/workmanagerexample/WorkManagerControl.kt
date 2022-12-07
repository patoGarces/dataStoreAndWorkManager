package com.example.workmanagerexample

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Transformations.map
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant

class WorkManagerControl(appContext: Context, workerParams: WorkerParameters) : Worker( appContext, workerParams) {

    private val TAG = "WorkManagerControlClass"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {

        GlobalScope.launch {
            contador = applicationContext.dataStore.data
                .map { preferences ->
                    preferences[intPreferencesKey("actualCont")] ?: 0
                }
                .first()

            contador++

            applicationContext.dataStore.edit{ preferences ->
                preferences[intPreferencesKey("actualCont")] = contador
                preferences[stringPreferencesKey("timeStamp")] = Instant.now().toString()
                preferences[intPreferencesKey("statusInt")] = StatusRequest.RUNNING.ordinal
            }
        }

        Log.d(TAG,"Contador: $contador,time: ${Instant.now()}")

        if(contador<5){
            return Result.retry()
        }
        else{
            GlobalScope.launch {
                applicationContext.dataStore.edit { preferences->
                    preferences[intPreferencesKey("statusInt")] = StatusRequest.FINISHED.ordinal
                }
            }
            return Result.success()
        }
    }

    companion object{
        var contador = 0
    }
}