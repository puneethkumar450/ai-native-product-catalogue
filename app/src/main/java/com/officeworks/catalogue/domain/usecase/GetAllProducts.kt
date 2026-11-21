package com.officeworks.catalogue.domain.usecase

import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.repository.ProductRepository

class GetAllProducts(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<Product> = repository.getAllProducts()
}
