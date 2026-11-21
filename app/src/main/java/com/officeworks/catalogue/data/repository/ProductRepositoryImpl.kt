package com.officeworks.catalogue.data.repository

import com.officeworks.catalogue.data.remote.CatalogueApi
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.model.EnquiryRequest
import com.officeworks.catalogue.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(private val api: CatalogueApi) : ProductRepository {
    override suspend fun getFeaturedProducts(): List<Product> = withContext(Dispatchers.IO) {
        api.getFeaturedProducts()
    }

    override suspend fun getCategories(): List<String> = withContext(Dispatchers.IO) {
        api.getCategories()
    }

    override suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        api.getProductsByCategory(category)
    }

    override suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        api.searchProducts(query)
    }

    override suspend fun getProduct(id: Int): Product = withContext(Dispatchers.IO) {
        api.getProduct(id)
    }

    override suspend fun submitEnquiry(request: EnquiryRequest): String = withContext(Dispatchers.IO) {
        api.submitEnquiry(request)
    }
}
