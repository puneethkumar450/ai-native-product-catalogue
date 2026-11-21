package com.officeworks.catalogue.presentation.home

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.presentation.components.CatalogueTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    onProductClick: (Int) -> Unit,
    onBackToHome: () -> Unit,
    onClearSearch: () -> Unit,
) {
    var searchText by rememberSaveable(state.searchQuery) { mutableStateOf(state.searchQuery.orEmpty()) }
    val category = state.selectedCategory
    val isSearchResults = state.searchQuery != null
    val isAllProducts = state.isShowingAllProducts
    Scaffold(
        topBar = {
            CatalogueTopAppBar(
                title = when {
                    isSearchResults -> "Search results"
                    isAllProducts -> "All products"
                    category != null -> category.replaceFirstChar { it.titlecase() }
                    else -> "Catalogue"
                },
                onNavigateBack = if (isSearchResults || isAllProducts || category != null) onBackToHome else null,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        Column(Modifier.fillMaxSize().padding(contentPadding)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                if (isSearchResults) {
                    Text("Results for ‘${state.searchQuery}’", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (isAllProducts) {
                    Text("Browse the complete catalogue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (category == null) {
                    Text("A better way to browse", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Find something\nyou’ll love.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))
                    Text("Featured products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("A few favourites to get you started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.categories.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        Text("Shop by category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.categories.forEach { item ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onCategorySelected(item) },
                                    label = { Text(item.replaceFirstChar { it.titlecase() }) },
                                )
                            }
                        }
                    }
                    state.categoriesError?.let { message ->
                        Spacer(Modifier.height(8.dp))
                        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetry) { Text("Retry categories") }
                    }
                } else {
                    Text("Top rated and more", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (category == null) {
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Search products") },
                            singleLine = true,
                            trailingIcon = {
                                if (searchText.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchText = ""
                                        onClearSearch()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                        )
                        Button(onClick = { onSearch(searchText) }, enabled = searchText.isNotBlank()) { Text("Search") }
                    }
                }
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Column(
                    Modifier.fillMaxSize().weight(1f).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.error)
                    TextButton(onClick = onRetry) { Text("Try again") }
                }
                else -> LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    val products = when {
                        isSearchResults -> state.searchResults
                        isAllProducts -> state.allProducts
                        category != null -> state.categoryProducts
                        else -> state.featuredProducts
                    }
                    if (products.isEmpty()) item { Text("No matching products found.", Modifier.padding(top = 12.dp)) }
                    items(products, key = Product::id) { product -> ProductCard(product) }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(product.imageUrl, size = 104.dp)
            Column(Modifier.weight(1f)) {
                Text(product.category.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(7.dp))
                Text(product.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(NumberFormat.getCurrencyInstance(Locale.US).format(product.price), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    product.rating?.let { Text("★ ${"%.1f".format(it)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }
                }
            }
        }
    }
}

@Composable
fun ProductImage(url: String, size: Dp = 92.dp) {
    val bitmap = remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) {
        bitmap.value = withContext(Dispatchers.IO) {
            runCatching {
                (URL(url).openConnection() as HttpURLConnection).run {
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    try { inputStream.use(BitmapFactory::decodeStream) } finally { disconnect() }
                }
            }.getOrNull()
        }
    }
    Box(Modifier.size(size).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
        bitmap.value?.let { Image(it.asImageBitmap(), null, Modifier.size(size * 0.82f), contentScale = ContentScale.Fit) }
    }
}
