package com.officeworks.catalogue.data.remote

import com.officeworks.catalogue.domain.model.Product
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CatalogueApi(private val baseUrl: String) {
    fun getFeaturedProducts(): List<Product> = getProducts("/api/products/featured")

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
