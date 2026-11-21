package com.officeworks.catalogue.domain.repository

import com.officeworks.catalogue.domain.model.Product

interface ProductRepository {
    suspend fun getAllProducts(): List<Product>
    suspend fun getFeaturedProducts(): List<Product>
    suspend fun getCategories(): List<String>
    suspend fun getProductsByCategory(category: String): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    suspend fun getProduct(id: Int): Product
}
