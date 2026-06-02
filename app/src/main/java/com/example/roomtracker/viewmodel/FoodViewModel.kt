package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.ContentRepository
import com.example.roomtracker.model.MenuItemJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    sealed class FoodState {
        object Loading : FoodState()
        data class Success(val items: List<MenuItemJson>) : FoodState()
        data class Error(val message: String) : FoodState()
    }

    private val repository = ContentRepository()

    private val _state = MutableStateFlow<FoodState>(FoodState.Loading)
    val state: StateFlow<FoodState> = _state.asStateFlow()

    init { loadMenu() }

    fun loadMenu() {
        viewModelScope.launch {
            _state.value = FoodState.Loading
            try {
                val carpeta = getApplication<Application>()
                    .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    .getString("carpeta_mapas", null)
                    ?: throw Exception("Institución no encontrada")
                _state.value = FoodState.Success(repository.loadMenu(carpeta))
            } catch (e: Exception) {
                android.util.Log.e("FOOD", "Error: ${e.message}")
                _state.value = FoodState.Error("No se pudo cargar el menú")
            }
        }
    }
}
