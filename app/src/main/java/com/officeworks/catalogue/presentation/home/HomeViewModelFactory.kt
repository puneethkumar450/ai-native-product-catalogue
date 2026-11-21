package com.officeworks.catalogue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts

class HomeViewModelFactory(private val getFeaturedProducts: GetFeaturedProducts) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(getFeaturedProducts) as T
    }
}
