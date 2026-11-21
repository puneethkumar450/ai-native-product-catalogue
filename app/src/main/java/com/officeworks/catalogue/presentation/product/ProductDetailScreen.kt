package com.officeworks.catalogue.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.presentation.home.ProductImage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailScreen(
    product: Product?,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onEnquire: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        TextButton(onClick = onBack) { Text("‹  Back to products") }
        when {
            isLoading -> Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            product == null -> Column(
                Modifier.fillMaxSize().weight(1f).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { Text(error ?: "Product unavailable") }
            else -> {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(12.dp))
                    ProductImage(product.imageUrl, size = 240.dp)
                    Spacer(Modifier.height(24.dp))
                    Text(product.category.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(product.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(NumberFormat.getCurrencyInstance(Locale.US).format(product.price), style = MaterialTheme.typography.titleLarge)
                        product.rating?.let { Text("★ ${"%.1f".format(it)}", color = MaterialTheme.colorScheme.secondary) }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(product.description, style = MaterialTheme.typography.bodyLarge)
                }
                Button(onClick = onEnquire, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text("Enquire about this product")
                }
            }
        }
    }
}
