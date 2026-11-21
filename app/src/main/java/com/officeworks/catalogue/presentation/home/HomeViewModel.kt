package com.officeworks.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredProducts: List<Product> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(private val getFeaturedProducts: GetFeaturedProducts) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init { loadFeaturedProducts() }

    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            runCatching { getFeaturedProducts() }
                .onSuccess { products -> _uiState.value = HomeUiState(isLoading = false, featuredProducts = products) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load catalogue. Check the API URL and try again.") }
        }
    }

    fun retry() {
        loadFeaturedProducts()
    }
}
