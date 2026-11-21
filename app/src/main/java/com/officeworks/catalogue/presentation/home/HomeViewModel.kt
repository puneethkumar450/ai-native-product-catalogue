package com.officeworks.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.model.EnquiryRequest
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import com.officeworks.catalogue.domain.usecase.GetCategories
import com.officeworks.catalogue.domain.usecase.GetProductsByCategory
import com.officeworks.catalogue.domain.usecase.SearchProducts
import com.officeworks.catalogue.domain.usecase.GetProductDetails
import com.officeworks.catalogue.domain.usecase.SubmitEnquiry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredProducts: List<Product> = emptyList(),
    val categoryProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String? = null,
    val searchResults: List<Product> = emptyList(),
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
    private val getProductDetails: GetProductDetails,
    private val submitEnquiry: SubmitEnquiry,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init { loadFeaturedProducts() }

    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = null, searchQuery = null)
            runCatching { getFeaturedProducts() to getCategories() }
                .onSuccess { (products, categories) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        featuredProducts = products,
                        categories = categories,
                        categoryProducts = emptyList(),
                        searchResults = emptyList(),
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load catalogue. Check the API URL and try again.") }
        }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = category, searchQuery = null)
            runCatching { getProductsByCategory(category) }
                .onSuccess { products -> _uiState.value = _uiState.value.copy(isLoading = false, categoryProducts = products) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Couldn't load this category. Please try again.") }
        }
    }

    fun search(query: String) {
        val term = query.trim()
        if (term.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = null, searchQuery = term)
            runCatching { searchProducts(term) }
                .onSuccess { products -> _uiState.value = _uiState.value.copy(isLoading = false, searchResults = products) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Search couldn't be completed. Please try again.") }
        }
    }

    fun retry() {
        val category = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery
        if (query != null) search(query)
        else if (category == null) loadFeaturedProducts()
        else selectCategory(category)
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
        _uiState.value = _uiState.value.copy(
            selectedProduct = null,
            isProductLoading = false,
            productError = null,
            isEnquiryOpen = false,
            isSubmittingEnquiry = false,
            enquiryMessage = null,
            enquiryError = null,
        )
    }

    fun openEnquiry() {
        _uiState.value = _uiState.value.copy(
            isEnquiryOpen = true,
            isSubmittingEnquiry = false,
            enquiryMessage = null,
            enquiryError = null,
        )
    }

    fun closeEnquiry() {
        _uiState.value = _uiState.value.copy(
            isEnquiryOpen = false,
            isSubmittingEnquiry = false,
            enquiryMessage = null,
            enquiryError = null,
        )
    }

    fun sendEnquiry(name: String, email: String, message: String) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingEnquiry = true, enquiryError = null)
            runCatching { submitEnquiry(EnquiryRequest(product.id, name.trim(), email.trim(), message.trim())) }
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(isSubmittingEnquiry = false, enquiryMessage = response)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingEnquiry = false,
                        enquiryError = error.message?.takeIf(String::isNotBlank) ?: "Couldn't submit your enquiry. Please try again.",
                    )
                }
        }
    }
}
