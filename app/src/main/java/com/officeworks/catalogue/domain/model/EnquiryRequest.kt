package com.officeworks.catalogue.domain.model

data class EnquiryRequest(
    val productId: Int,
    val name: String,
    val email: String,
    val message: String,
)
