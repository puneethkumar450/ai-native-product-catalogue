package com.officeworks.catalogue.domain.usecase

import com.officeworks.catalogue.domain.repository.ProductRepository

class GetCategories(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<String> = repository.getCategories()
}
