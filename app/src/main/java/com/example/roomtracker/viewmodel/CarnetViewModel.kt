package com.example.roomtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.AcademicRepository
import com.example.roomtracker.model.CarnetData
import com.example.roomtracker.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarnetViewModel(application: Application) : AndroidViewModel(application) {

    sealed class CarnetState {
        object Loading : CarnetState()
        data class Success(val data: CarnetData) : CarnetState()
        data class Error(val message: String) : CarnetState()
    }

    private val repository = AcademicRepository()

    private val _state = MutableStateFlow<CarnetState>(CarnetState.Loading)
    val state: StateFlow<CarnetState> = _state.asStateFlow()

    init { loadCarnet() }

    fun loadCarnet() {
        viewModelScope.launch {
            _state.value = CarnetState.Loading
            try {
                val uid = supabase.auth.currentUserOrNull()?.id
                    ?: throw Exception("No hay sesión activa")
                _state.value = CarnetState.Success(repository.loadCarnet(uid))
            } catch (e: Exception) {
                android.util.Log.e("CARNET", "Error: ${e.message}")
                _state.value = CarnetState.Error(e.message ?: "Error al cargar el carnet")
            }
        }
    }
}
