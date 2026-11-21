package com.officeworks.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import com.officeworks.catalogue.domain.usecase.GetCategories
import com.officeworks.catalogue.domain.usecase.GetProductsByCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredProducts: List<Product> = emptyList(),
    val categoryProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val error: String? = null,
)

class HomeViewModel(
    private val getFeaturedProducts: GetFeaturedProducts,
    private val getCategories: GetCategories,
    private val getProductsByCategory: GetProductsByCategory,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init { loadFeaturedProducts() }

    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            runCatching { getFeaturedProducts() to getCategories() }
                .onSuccess { (products, categories) -> _uiState.value = HomeUiState(isLoading = false, featuredProducts = products, categories = categories) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load catalogue. Check the API URL and try again.") }
        }
    }

    fun retry() {
        val category = _uiState.value.selectedCategory
        if (category == null) loadFeaturedProducts() else selectCategory(category)
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = category)
            runCatching { getProductsByCategory(category) }
                .onSuccess { products -> _uiState.value = _uiState.value.copy(isLoading = false, categoryProducts = products) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load this category. Please try again.") }
        }
    }

    fun backToHome() {
        _uiState.value = _uiState.value.copy(isLoading = false, error = null, selectedCategory = null)
    }
}
