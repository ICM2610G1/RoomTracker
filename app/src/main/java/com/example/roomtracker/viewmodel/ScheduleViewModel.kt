package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.model.ScheduleTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.scheduleDataStore by preferencesDataStore(name = "schedule")

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore  = application.scheduleDataStore
    private val TASKS_KEY  = stringPreferencesKey("tasks")
    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val _tasks = MutableStateFlow<List<ScheduleTask>>(emptyList())
    val tasks: StateFlow<List<ScheduleTask>> = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val raw = prefs[TASKS_KEY] ?: "[]"
                _tasks.value = try { jsonParser.decodeFromString(raw) } catch (_: Exception) { emptyList() }
            }
        }
    }

    fun addTask(task: ScheduleTask)    = persist(_tasks.value + task)
    fun updateTask(task: ScheduleTask) = persist(_tasks.value.map { if (it.id == task.id) task else it })
    fun deleteTask(id: String)         = persist(_tasks.value.filter { it.id != id })

    private fun persist(list: List<ScheduleTask>) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[TASKS_KEY] = jsonParser.encodeToString(list) }
        }
    }
}
