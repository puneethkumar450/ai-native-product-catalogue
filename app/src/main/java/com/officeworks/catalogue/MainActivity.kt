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
import androidx.compose.ui.graphics.Color
import com.officeworks.catalogue.data.remote.CatalogueApi
import com.officeworks.catalogue.data.repository.ProductRepositoryImpl
import com.officeworks.catalogue.domain.usecase.GetFeaturedProducts
import com.officeworks.catalogue.domain.usecase.GetCategories
import com.officeworks.catalogue.domain.usecase.GetProductsByCategory
import com.officeworks.catalogue.domain.usecase.SearchProducts
import com.officeworks.catalogue.domain.usecase.GetAllProducts
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
            GetAllProducts(repository),
            GetProductDetails(repository),
            SubmitEnquiry(repository),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF155E59),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFD0EEE8),
                    onPrimaryContainer = Color(0xFF0B332F),
                    secondary = Color(0xFF5E625F),
                    background = Color(0xFFF5F7F6),
                    surface = Color(0xFFFFFEFC),
                    surfaceVariant = Color(0xFFE8EEEB),
                ),
            ) {
                val state by viewModel.uiState.collectAsState()
                val enquiryOpen = state.isEnquiryOpen && state.selectedProduct != null
                val productOpen = state.selectedProduct != null || state.isProductLoading || state.productError != null
                BackHandler(enabled = enquiryOpen || productOpen) { if (enquiryOpen) viewModel.closeEnquiry() else viewModel.closeProduct() }
                if (enquiryOpen) EnquiryScreen(
                    product = state.selectedProduct!!,
                    isSubmitting = state.isSubmittingEnquiry,
                    thankYouMessage = state.enquiryMessage,
                    error = state.enquiryError,
                    onBack = viewModel::closeEnquiry,
                    onSubmit = viewModel::submitEnquiry,
                ) else if (productOpen) ProductDetailScreen(
                    product = state.selectedProduct,
                    isLoading = state.isProductLoading,
                    error = state.productError,
                    onBack = viewModel::closeProduct,
                    onEnquire = viewModel::openEnquiry,
                ) else HomeScreen(
                    state = state,
                    onRetry = viewModel::retry,
                    onCategorySelected = viewModel::selectCategory,
                    onBackToHome = viewModel::backToHome,
                    onSearch = viewModel::search,
                    onClearSearch = viewModel::showAllProducts,
                    onProductClick = viewModel::openProduct,
                )
            }
        }
    }
}
