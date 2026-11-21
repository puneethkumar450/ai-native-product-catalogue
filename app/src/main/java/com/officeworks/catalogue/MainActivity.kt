package com.officeworks.catalogue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.officeworks.catalogue.presentation.home.HomeScreen
import com.officeworks.catalogue.presentation.home.HomeViewModel
import com.officeworks.catalogue.presentation.home.HomeViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels {
        val repository = ProductRepositoryImpl(CatalogueApi(BuildConfig.API_BASE_URL))
        HomeViewModelFactory(
            GetFeaturedProducts(repository),
            GetCategories(repository),
            GetProductsByCategory(repository),
            SearchProducts(repository),
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
                HomeScreen(
                    state = state,
                    onRetry = viewModel::retry,
                    onCategorySelected = viewModel::selectCategory,
                    onBackToHome = viewModel::backToHome,
                    onSearch = viewModel::search,
                )
            }
        }
    }
}
