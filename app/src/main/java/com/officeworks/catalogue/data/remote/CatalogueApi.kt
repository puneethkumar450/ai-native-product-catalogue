package com.officeworks.catalogue.data.remote

import android.net.Uri
import com.officeworks.catalogue.domain.model.EnquiryRequest
import com.officeworks.catalogue.domain.model.Product
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CatalogueApi(private val baseUrl: String) {
    fun getAllProducts(): List<Product> = getProducts("/api/products")

    fun getFeaturedProducts(): List<Product> = getProducts("/api/products/featured")

    fun getCategories(): List<String> {
        val json = JSONArray(get("/api/categories"))
        return List(json.length()) { index -> json.getString(index) }
    }

    fun getProductsByCategory(category: String): List<Product> =
        getProducts("/api/categories/${Uri.encode(category)}/products")

    fun searchProducts(query: String): List<Product> =
        getProducts("/api/products?q=${Uri.encode(query)}")

    fun getProduct(id: Int): Product = JSONObject(get("/api/products/$id")).toProduct()

    fun submitEnquiry(request: EnquiryRequest): String {
        val body = JSONObject()
            .put("productId", request.productId)
            .put("name", request.name)
            .put("email", request.email)
            .put("message", request.message)
        return JSONObject(post("/api/enquiries", body.toString())).optString("message")
    }

    private fun getProducts(path: String): List<Product> = JSONArray(get(path)).toProducts()

    private fun get(path: String): String {
        val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (status !in 200..299) error("Catalogue API returned HTTP $status")
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun post(path: String, requestBody: String): String {
        val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 8_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        try {
            connection.outputStream.bufferedWriter().use { it.write(requestBody) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (status !in 200..299) {
                val message = runCatching { JSONObject(body).optString("error") }.getOrDefault("")
                error(message.ifBlank { "Catalogue API returned HTTP $status" })
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONArray.toProducts(): List<Product> = buildList {
        for (index in 0 until length()) {
            val json = getJSONObject(index)
            add(json.toProduct())
        }
    }

    private fun JSONObject.toProduct() = Product(
        id = getInt("id"),
        title = getString("title"),
        price = getDouble("price"),
        description = optString("description"),
        category = optString("category"),
        imageUrl = optString("image"),
        rating = optJSONObject("rating")?.optDouble("rate")?.takeUnless { it.isNaN() },
    )
}
