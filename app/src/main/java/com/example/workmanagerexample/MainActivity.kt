package com.example.workmanagerexample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// funcion de extension
val Context.dataStore by preferencesDataStore(name = "CONT_ACTUAL") // el delegado permite que sea un singletone, una sola instancia

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        registerListener()
    }

    private fun registerListener() {
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val btnResetCont = findViewById<Button>(R.id.btnResetCont)
        val btnSetRandom = findViewById<Button>(R.id.btnSetRandom)
        val tvContador = findViewById<TextView>(R.id.tvCont)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvLastUpdate = findViewById<TextView>(R.id.tvLastUpdate)

        lifecycleScope.launch(Dispatchers.IO){
            getActualCont().collect{

                withContext(Dispatchers.Main) {
                    tvContador.text = it.cont.toString()
                    tvLastUpdate.text = it.timeStamp
                    //tvStatus.text = it.status

                    tvStatus.text = mapStatusText(it.statusInt)
                    tvStatus.setTextColor(getColor(mapStatusColor(it.statusInt)))

                }
            }
        }

        btnSetRandom.setOnClickListener {
            var randomNumber = (0..100).random()
            lifecycleScope.launch(Dispatchers.IO) {
                saveActualCont(randomNumber, "nada", StatusRequest.INIT.ordinal)
            }
            tvContador.text = randomNumber.toString()
        }

        btnResetCont.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                saveActualCont(0, "nada",StatusRequest.INIT.ordinal)
            }
            tvContador.text = "0"
        }

        btnStart.setOnClickListener {

            setOneTimeWork()
            lifecycleScope.launch(Dispatchers.IO) {
                saveStatus(StatusRequest.RUNNING.ordinal)
            }
            Toast.makeText(this,"Servicio creado",Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            WorkManager.getInstance(this).cancelAllWork()
            lifecycleScope.launch(Dispatchers.IO) {
                saveStatus(StatusRequest.CANCELLED.ordinal)
            }
            Toast.makeText(this,"Servicios en segundo plano eliminados!",Toast.LENGTH_SHORT).show()
        }
    }

    fun setOneTimeWork(){
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(false)
            .build()

        val myWorkRequest = OneTimeWorkRequest.Builder(WorkManagerControl :: class.java)
//            .setInitialDelay()                        // para setear un delay antes de ejecutar la tarea la primera vez
            .setConstraints(constraint)
            .setBackoffCriteria(BackoffPolicy.LINEAR ,OneTimeWorkRequest.MIN_BACKOFF_MILLIS,TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(myWorkRequest)
    }

    fun setPeriodicWork(tag : String){
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(false)
            .build()

        val myWorkRequest = PeriodicWorkRequest.Builder(WorkManagerControl :: class.java,15,TimeUnit.MINUTES)       // TIEMPO MINIMO DE REPETICION: 15MIN, no importa si se setea menos, minimo 15 min.
//            .setInitialDelay()                        // para setear un delay antes de ejecutar la tarea la primera vez
            .setConstraints(constraint)
            //.setBackoffCriteria(BackoffPolicy.LINEAR ,OneTimeWorkRequest.MAX_BACKOFF_MILLIS,TimeUnit.SECONDS)
            .addTag(tag)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(tag,ExistingPeriodicWorkPolicy.KEEP,myWorkRequest)
    }

    private fun getActualCont() = dataStore.data.map{ preferences->
        UserData(
            cont = preferences[intPreferencesKey("actualCont")] ?: 0,
            timeStamp = preferences[stringPreferencesKey("timeStamp")].orEmpty(),
            statusInt = preferences[intPreferencesKey("statusInt")] ?: 0
        )
    }

    suspend fun saveActualCont(valActual : Int,timeStamp : String,statusInt: Int){

        dataStore.edit{ preferences ->
            preferences[intPreferencesKey("actualCont")] = valActual
            preferences[stringPreferencesKey("timeStamp")] = timeStamp
            preferences[intPreferencesKey("statusInt")] = statusInt
        }
    }

    suspend fun saveStatus(status : Int){
        dataStore.edit { preferences ->
            preferences[intPreferencesKey("statusInt")] = status
        }
    }
}