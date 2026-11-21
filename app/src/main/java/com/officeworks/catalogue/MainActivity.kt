package com.officeworks.catalogue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.officeworks.catalogue.data.remote.CatalogueApi
import com.officeworks.catalogue.data.repository.ProductRepositoryImpl
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import com.officeworks.catalogue.domain.usecase.GetCategories
import com.officeworks.catalogue.domain.usecase.GetProductsByCategory
import com.officeworks.catalogue.domain.usecase.SearchProducts
import com.officeworks.catalogue.domain.usecase.GetProductDetails
import com.officeworks.catalogue.domain.usecase.SubmitEnquiry
import com.officeworks.catalogue.presentation.home.HomeScreen
import com.officeworks.catalogue.presentation.home.HomeViewModel
import com.officeworks.catalogue.presentation.home.HomeViewModelFactory
import com.officeworks.catalogue.presentation.product.ProductDetailScreen
import com.officeworks.catalogue.presentation.product.EnquiryScreen

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels {
        val repository = ProductRepositoryImpl(CatalogueApi(BuildConfig.API_BASE_URL))
        HomeViewModelFactory(
            GetFeaturedProducts(repository),
            GetCategories(repository),
            GetProductsByCategory(repository),
            SearchProducts(repository),
            GetProductDetails(repository),
            SubmitEnquiry(repository),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val state by viewModel.uiState.collectAsState()
                val selectedProduct = state.selectedProduct
                BackHandler(enabled = selectedProduct != null || state.isProductLoading || state.productError != null) {
                    if (state.isEnquiryOpen) viewModel.closeEnquiry() else viewModel.closeProduct()
                }
                when {
                    selectedProduct != null && state.isEnquiryOpen -> EnquiryScreen(
                        product = selectedProduct,
                        isSubmitting = state.isSubmittingEnquiry,
                        thankYouMessage = state.enquiryMessage,
                        error = state.enquiryError,
                        onBack = viewModel::closeEnquiry,
                        onSubmit = viewModel::sendEnquiry,
                    )
                    selectedProduct != null || state.isProductLoading || state.productError != null -> ProductDetailScreen(
                        product = selectedProduct,
                        isLoading = state.isProductLoading,
                        error = state.productError,
                        onBack = viewModel::closeProduct,
                        onEnquire = viewModel::openEnquiry,
                    )
                    else -> HomeScreen(
                        state = state,
                        onRetry = viewModel::retry,
                        onCategorySelected = viewModel::selectCategory,
                        onSearch = viewModel::search,
                        onProductClick = viewModel::openProduct,
                        onBackToHome = viewModel::loadFeaturedProducts,
                    )
                }
            }
        }
    }
}
