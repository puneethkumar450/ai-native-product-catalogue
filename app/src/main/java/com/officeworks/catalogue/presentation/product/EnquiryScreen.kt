package com.officeworks.catalogue.presentation.product

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.presentation.components.CatalogueTopAppBar

@Composable
fun EnquiryScreen(
    product: Product,
    isSubmitting: Boolean,
    thankYouMessage: String?,
    error: String?,
    onBack: () -> Unit,
    onSubmit: (name: String, email: String, message: String) -> Unit,
) {
    var name by remember(product.id) { mutableStateOf("") }
    var email by remember(product.id) { mutableStateOf("") }
    var message by remember(product.id) { mutableStateOf("") }

    Scaffold(topBar = { CatalogueTopAppBar("Product enquiry", onNavigateBack = onBack) }, containerColor = MaterialTheme.colorScheme.background) { padding ->
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp)) {
        Spacer(Modifier.height(12.dp))
        if (thankYouMessage != null) {
            Column(
                Modifier.fillMaxWidth().weight(1f, fill = false).padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Thank you!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(thankYouMessage, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onBack) { Text("Done") }
            }
        } else {
            Text("Product enquiry", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(product.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your name") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email address") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Message (optional)") },
                minLines = 4,
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onSubmit(name, email, message) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && name.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches(),
            ) {
                if (isSubmitting) CircularProgressIndicator()
                else Text("Send enquiry")
            }
        }
    }
    }
}
