package com.example.roomtracker.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomtracker.data.repository.ContentRepository
import com.example.roomtracker.model.ProductoJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    sealed class ShopState {
        object Loading : ShopState()
        data class Success(val productos: List<ProductoJson>) : ShopState()
        data class Error(val message: String) : ShopState()
    }

    private val repository = ContentRepository()

    private val _state = MutableStateFlow<ShopState>(ShopState.Loading)
    val state: StateFlow<ShopState> = _state.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = ShopState.Loading
            try {
                val carpeta = getApplication<Application>()
                    .getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    .getString("carpeta_mapas", null)
                    ?: throw Exception("Institución no encontrada")
                _state.value = ShopState.Success(repository.loadProductos(carpeta))
            } catch (e: Exception) {
                android.util.Log.e("SHOP", "Error: ${e.message}")
                _state.value = ShopState.Error("No se pudieron cargar los productos")
            }
        }
    }
}
