package com.officeworks.catalogue.data.repository

import com.officeworks.catalogue.data.remote.CatalogueApi
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(private val api: CatalogueApi) : ProductRepository {
    override suspend fun getFeaturedProducts(): List<Product> = withContext(Dispatchers.IO) {
        api.getFeaturedProducts()
    }
}
