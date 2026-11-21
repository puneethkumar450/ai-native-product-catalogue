package com.officeworks.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import com.officeworks.catalogue.domain.usecase.GetCategories
import com.officeworks.catalogue.domain.usecase.GetProductsByCategory
import com.officeworks.catalogue.domain.usecase.SearchProducts
import com.officeworks.catalogue.domain.usecase.GetProductDetails

class HomeViewModelFactory(
    private val getFeaturedProducts: GetFeaturedProducts,
    private val getCategories: GetCategories,
    private val getProductsByCategory: GetProductsByCategory,
    private val searchProducts: SearchProducts,
    private val getProductDetails: GetProductDetails,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(getFeaturedProducts, getCategories, getProductsByCategory, searchProducts, getProductDetails) as T
    }
}
