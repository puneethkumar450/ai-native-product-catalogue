package com.officeworks.catalogue.domain.usecase

import com.officeworks.catalogue.domain.model.EnquiryRequest
import com.officeworks.catalogue.domain.repository.ProductRepository

class SubmitEnquiry(private val repository: ProductRepository) {
    suspend operator fun invoke(request: EnquiryRequest): String = repository.submitEnquiry(request)
}
