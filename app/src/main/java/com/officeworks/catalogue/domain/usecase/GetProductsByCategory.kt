package com.officeworks.catalogue.domain.usecase

import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.repository.ProductRepository

class GetProductsByCategory(private val repository: ProductRepository) {
    suspend operator fun invoke(category: String): List<Product> = repository.getProductsByCategory(category)
}
