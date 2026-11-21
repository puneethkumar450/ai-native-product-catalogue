package com.officeworks.catalogue.domain.repository

import com.officeworks.catalogue.domain.model.Product

interface ProductRepository {
    suspend fun getFeaturedProducts(): List<Product>
}
