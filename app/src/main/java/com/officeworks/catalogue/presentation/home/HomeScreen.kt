package com.officeworks.catalogue.presentation.home

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.officeworks.catalogue.domain.model.Product
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
) {
    var searchText by rememberSaveable(state.searchQuery) { mutableStateOf(state.searchQuery.orEmpty()) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            if (state.selectedCategory != null || state.searchQuery != null) {
                TextButton(onClick = onBackToHome) { Text("‹  All products") }
                Text(
                    state.selectedCategory?.replaceFirstChar { it.titlecase() } ?: "Search results",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (state.searchQuery != null) {
                    Text("Matching ‘${state.searchQuery}’", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Top rated and more", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text("CATALOGUE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Find something\nyou’ll love.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Browse our featured products", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (state.selectedCategory == null) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Search products") },
                        singleLine = true,
                    )
                    Button(onClick = { onSearch(searchText) }, enabled = searchText.isNotBlank()) {
                        Text("Search")
                    }
                }
            }

            if (state.selectedCategory == null && state.searchQuery == null && state.categories.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("Shop by category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = false,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category.replaceFirstChar { it.titlecase() }) },
                        )
                    }
                }
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Column(
                Modifier.fillMaxSize().weight(1f).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onRetry) { Text("Try again") }
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val products = when {
                    state.selectedCategory != null -> state.categoryProducts
                    state.searchQuery != null -> state.searchResults
                    else -> state.featuredProducts
                }
                if (products.isEmpty()) item { Text("No matching products found.", Modifier.padding(top = 12.dp)) }
                items(products, key = Product::id) { product -> ProductCard(product, onClick = { onProductClick(product.id) }) }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(product.imageUrl)
            Column(Modifier.weight(1f)) {
                Text(product.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(5.dp))
                Text(product.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(NumberFormat.getCurrencyInstance(Locale.US).format(product.price), style = MaterialTheme.typography.titleSmall)
                product.rating?.let { Text("★ ${"%.1f".format(it)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }
            }
        }
    }
}

@Composable
fun ProductImage(url: String, size: androidx.compose.ui.unit.Dp = 92.dp) {
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
    Box(
        Modifier.size(size).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        bitmap.value?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(size * 0.82f), contentScale = ContentScale.Fit)
        }
    }
}
