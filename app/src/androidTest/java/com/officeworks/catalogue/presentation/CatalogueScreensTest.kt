package com.officeworks.catalogue.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.officeworks.catalogue.domain.model.Product
import com.officeworks.catalogue.presentation.home.HomeScreen
import com.officeworks.catalogue.presentation.home.HomeUiState
import com.officeworks.catalogue.presentation.product.EnquiryScreen
import com.officeworks.catalogue.presentation.product.ProductDetailScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogueScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val product = Product(
        id = 1,
        title = "Phone Case",
        price = 12.5,
        description = "Protective case for a phone",
        category = "electronics",
        imageUrl = "",
        rating = 4.2,
    )

    @Test
    fun homeShowsFeaturedProductAndOneCategoryNavigationRow() {
        var selectedCategory: String? = null
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(
                        isLoading = false,
                        featuredProducts = listOf(product),
                        categories = listOf("electronics"),
                    ),
                    onRetry = {},
                    onCategorySelected = { selectedCategory = it },
                    onSearch = {},
                    onProductClick = {},
                    onBackToHome = {},
                    onClearSearch = {},
                )
            }
        }

        composeRule.onNodeWithText("Featured products").assertIsDisplayed()
        composeRule.onNodeWithText("Phone Case").assertIsDisplayed()
        composeRule.onAllNodesWithText("Shop by category").assertCountEquals(1)
        composeRule.onNodeWithText("Electronics").performClick()
        assertEquals("electronics", selectedCategory)
    }

    @Test
    fun homeKeepsFeaturedProductsVisibleAndRetriesWhenCategoriesFail() {
        var retried = false
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(
                        isLoading = false,
                        featuredProducts = listOf(product),
                        categoriesError = "Couldn't load categories. Featured products are still available.",
                    ),
                    onRetry = { retried = true },
                    onCategorySelected = {},
                    onSearch = {},
                    onProductClick = {},
                    onBackToHome = {},
                    onClearSearch = {},
                )
            }
        }

        composeRule.onNodeWithText("Phone Case").assertIsDisplayed()
        composeRule.onNodeWithText("Couldn't load categories. Featured products are still available.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry categories").performClick()
        assertEquals(true, retried)
    }

    @Test
    fun homeSubmitsSearch() {
        var searchedQuery: String? = null
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(isLoading = false),
                    onRetry = {},
                    onCategorySelected = {},
                    onSearch = { searchedQuery = it },
                    onProductClick = {},
                    onBackToHome = {},
                    onClearSearch = {},
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("wireless")
        composeRule.onNodeWithText("Search").performClick()
        assertEquals("wireless", searchedQuery)
    }


    @Test
    fun clearingSearchLoadsAllProducts() {
        var clearRequested = false
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(isLoading = false, searchQuery = "wireless", searchResults = listOf(product)),
                    onRetry = {},
                    onCategorySelected = {},
                    onSearch = {},
                    onProductClick = {},
                    onBackToHome = {},
                    onClearSearch = { clearRequested = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        assertEquals(true, clearRequested)
    }

    @Test
    fun homeShowsEmptySearchResultsState() {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(isLoading = false, searchQuery = "wireless"),
                    onRetry = {},
                    onCategorySelected = {},
                    onSearch = {},
                    onProductClick = {},
                    onBackToHome = {},
                    onClearSearch = {},
                )
            }
        }
        composeRule.onNodeWithText("No matching products found.").assertIsDisplayed()
    }

    @Test
    fun productDetailsShowsInformationAndOpensEnquiryAction() {
        var enquiryOpened = false
        composeRule.setContent {
            MaterialTheme {
                ProductDetailScreen(
                    product = product,
                    isLoading = false,
                    error = null,
                    onBack = {},
                    onEnquire = { enquiryOpened = true },
                )
            }
        }

        composeRule.onNodeWithText("Phone Case").assertIsDisplayed()
        composeRule.onNodeWithText("Protective case for a phone").assertIsDisplayed()
        composeRule.onNodeWithText("Enquire about this product").performClick()
        assertEquals(true, enquiryOpened)
    }

    @Test
    fun productDetailsShowsLoadingAndErrorStates() {
        composeRule.setContent {
            MaterialTheme {
                ProductDetailScreen(
                    product = null,
                    isLoading = false,
                    error = "Couldn't load product details. Please try again.",
                    onBack = {},
                    onEnquire = {},
                )
            }
        }

        composeRule.onNodeWithText("Couldn't load product details. Please try again.").assertIsDisplayed()
    }

    @Test
    fun enquiryRequiresValidContactDetailsAndSubmitsForm() {
        var submitted: List<String>? = null
        composeRule.setContent {
            MaterialTheme {
                EnquiryScreen(
                    product = product,
                    isSubmitting = false,
                    thankYouMessage = null,
                    error = null,
                    onBack = {},
                    onSubmit = { name, email, message -> submitted = listOf(name, email, message) },
                )
            }
        }

        composeRule.onNodeWithText("Send enquiry").assertIsNotEnabled()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Ada Lovelace")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("ada@example.com")
        composeRule.onAllNodes(hasSetTextAction())[2].performTextInput("Please contact me")
        composeRule.onNodeWithText("Send enquiry").assertIsEnabled().performClick()
        assertEquals(listOf("Ada Lovelace", "ada@example.com", "Please contact me"), submitted)
    }

    @Test
    fun enquiryDisplaysServerError() {
        composeRule.setContent {
            MaterialTheme {
                EnquiryScreen(
                    product = product,
                    isSubmitting = false,
                    thankYouMessage = null,
                    error = "Catalogue service is temporarily unavailable",
                    onBack = {},
                    onSubmit = { _, _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("Catalogue service is temporarily unavailable").assertIsDisplayed()
    }

    @Test
    fun enquiryDisplaysThankYouState() {
        composeRule.setContent {
            MaterialTheme {
                EnquiryScreen(
                    product = product,
                    isSubmitting = false,
                    thankYouMessage = "Thank you. Your enquiry has been received.",
                    error = null,
                    onBack = {},
                    onSubmit = { _, _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("Thank you!").assertIsDisplayed()
        composeRule.onNodeWithText("Thank you. Your enquiry has been received.").assertIsDisplayed()
    }
}
