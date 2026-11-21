package com.officeworks.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import com.officeworks.catalogue.domain.usecase.GetCategories
import com.officeworks.catalogue.domain.usecase.GetProductsByCategory
import com.officeworks.catalogue.domain.usecase.SearchProducts
import com.officeworks.catalogue.domain.usecase.GetAllProducts
import com.officeworks.catalogue.domain.usecase.GetProductDetails
import com.officeworks.catalogue.domain.model.EnquiryRequest
import com.officeworks.catalogue.domain.usecase.SubmitEnquiry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredProducts: List<Product> = emptyList(),
    val categoryProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val categoriesError: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String? = null,
    val searchResults: List<Product> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val isShowingAllProducts: Boolean = false,
    val selectedProduct: Product? = null,
    val isProductLoading: Boolean = false,
    val productError: String? = null,
    val isEnquiryOpen: Boolean = false,
    val isSubmittingEnquiry: Boolean = false,
    val enquiryMessage: String? = null,
    val enquiryError: String? = null,
    val error: String? = null,
)

class HomeViewModel(
    private val getFeaturedProducts: GetFeaturedProducts,
    private val getCategories: GetCategories,
    private val getProductsByCategory: GetProductsByCategory,
    private val searchProducts: SearchProducts,
    private val getAllProducts: GetAllProducts,
    private val getProductDetails: GetProductDetails,
    private val submitEnquiry: SubmitEnquiry,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private var catalogueRequestId = 0L

    init { loadFeaturedProducts() }

    fun loadFeaturedProducts() {
        val requestId = ++catalogueRequestId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = null, searchQuery = null, isShowingAllProducts = false)
            val featuredResult = runCatching { getFeaturedProducts() }
            if (requestId != catalogueRequestId) return@launch
            val products = featuredResult.getOrElse {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load featured products. Check the API URL and try again.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                featuredProducts = products,
                categoryProducts = emptyList(),
                categories = emptyList(),
                categoriesError = null,
                searchResults = emptyList(),
                allProducts = emptyList(),
                isShowingAllProducts = false,
            )

            val categoriesResult = runCatching { getCategories() }
            if (requestId != catalogueRequestId) return@launch
            categoriesResult.onSuccess { categories ->
                _uiState.value = _uiState.value.copy(categories = categories, categoriesError = null)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    categoriesError = "Couldn't load categories. Featured products are still available.",
                )
            }
        }
    }

    fun retry() {
        val category = _uiState.value.selectedCategory
        if (category == null) loadFeaturedProducts() else selectCategory(category)
    }

    fun selectCategory(category: String) {
        val requestId = ++catalogueRequestId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = category, searchQuery = null, isShowingAllProducts = false)
            val result = runCatching { getProductsByCategory(category) }
            if (requestId != catalogueRequestId || _uiState.value.selectedCategory != category) return@launch
            result.onSuccess { products -> _uiState.value = _uiState.value.copy(isLoading = false, categoryProducts = products) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load this category. Please try again.") }
        }
    }

    fun search(query: String) {
        val term = query.trim()
        if (term.isEmpty()) return
        val requestId = ++catalogueRequestId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = null, searchQuery = term, isShowingAllProducts = false)
            val result = runCatching { searchProducts(term) }
            if (requestId != catalogueRequestId || _uiState.value.searchQuery != term) return@launch
            result.onSuccess { products -> _uiState.value = _uiState.value.copy(isLoading = false, searchResults = products) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Search couldn't be completed. Please try again.") }
        }
    }

    fun showAllProducts() {
        val requestId = ++catalogueRequestId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = null, searchQuery = null, isShowingAllProducts = true)
            runCatching { getAllProducts() }
                .onSuccess { products ->
                    if (requestId == catalogueRequestId) _uiState.value = _uiState.value.copy(isLoading = false, allProducts = products)
                }
                .onFailure {
                    if (requestId == catalogueRequestId) _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load products. Please try again.")
                }
        }
    }

    fun backToHome() {
        catalogueRequestId += 1
        _uiState.value = _uiState.value.copy(isLoading = false, error = null, selectedCategory = null, searchQuery = null, isShowingAllProducts = false)
    }

    fun openProduct(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProductLoading = true, productError = null)
            runCatching { getProductDetails(id) }
                .onSuccess { product -> _uiState.value = _uiState.value.copy(isProductLoading = false, selectedProduct = product) }
                .onFailure { _uiState.value = _uiState.value.copy(isProductLoading = false, productError = "Couldn't load product details. Please try again.") }
        }
    }

    fun closeProduct() {
        _uiState.value = _uiState.value.copy(selectedProduct = null, isProductLoading = false, productError = null)
    }

}
