package com.officeworks.catalogue.domain.usecase

import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.repository.ProductRepository

class SearchProducts(private val repository: ProductRepository) {
    suspend operator fun invoke(query: String): List<Product> = repository.searchProducts(query)
}
