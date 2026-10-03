package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.MutableCategory
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MutableCategoryTest {
    @Test
    fun `test mutable category creation with valid data`() {
        val category = MutableCategory("Potluck")
        assertEquals(category.name, "Potluck")
    }

    @Test
    fun `test addItem adds an item to the category`() {
        val category = MutableCategory("Potluck")
        category.addItem("Luna", "item")
        assertEquals(category.items["item"], "Luna")
    }

    @Test
    fun `test removeItem by the user who added it removes the item from the category`() {
        val category = MutableCategory("Potluck")
        category.addItem("Luna", "item")
        category.removeItem("Luna", "item")
        assertNull(category.items["item"])
    }

    @Test
    fun `test removeItem by a user who didnt' add it doesn't remove the item from the category`() {
        val category = MutableCategory("Potluck")
        category.addItem("Luna", "item")
        assertFailsWith<IllegalArgumentException>("Cannot remove item from list") {
            category.removeItem(
                "Harbin",
                "item"
            )
        }
    }

}