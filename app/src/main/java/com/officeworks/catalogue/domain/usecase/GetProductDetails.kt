package com.officeworks.catalogue.domain.usecase

import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.repository.ProductRepository

class GetProductDetails(private val repository: ProductRepository) {
    suspend operator fun invoke(id: Int): Product = repository.getProduct(id)
}
