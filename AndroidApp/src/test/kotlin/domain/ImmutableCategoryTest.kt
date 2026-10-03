package cs.uwaterloo.domain

import cs.uwaterloo.domain.entity.Attendee
import cs.uwaterloo.domain.entity.ImmutableCategory
import cs.uwaterloo.domain.entity.Role
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ImmutableCategoryTest {

    @Test
    fun `test ImmutableCategory creation with valid data`() {
        val category = ImmutableCategory("Wishlist")
        assertEquals(category.name, "Wishlist")
    }

    @Test
    fun `test addItem by a host adds an item to the category list`() {
        val category = ImmutableCategory("Wishlist")
        val attendee = Attendee(userId = "user123", name = "Luna", role = Role.HOST)
        category.addItem(attendee, "item")
        assertEquals(category.items["item"], mutableSetOf())
    }

    @Test
    fun `test addItem by an invitee doesn't add an item to the category list`() {
        val category = ImmutableCategory("Wishlist")
        val attendee = Attendee(userId = "user123", name = "Luna", role = Role.INVITEE)
        assertFailsWith<IllegalArgumentException>("This attendee is not a host or co-host")
        { category.addItem(attendee, "item") }
    }

    @Test
    fun `test checkItem checks an item`() {
        val category = ImmutableCategory("Wishlist")
        val host = Attendee(userId = "user123", name = "Luna", role = Role.HOST)
        category.addItem(host, "item")
        val guest = Attendee(userId = "user567", name = "Harbin", role = Role.GUEST)
        category.checkItem("Harbin", "item")
        assertEquals(category.items["item"], mutableSetOf(guest.name))
    }

    @Test
    fun `test getCheckedUsers returns the users who checked the given item`() {
        val category = ImmutableCategory("Wishlist")
        val host = Attendee(userId = "user123", name = "Luna", role = Role.HOST)
        category.addItem(host, "item")
        val guest = Attendee(userId = "user567", name = "Harbin", role = Role.GUEST)
        category.checkItem("Harbin", "item")
        assertEquals(mutableSetOf(guest.name), category.getCheckedUsers("item"))
    }

    @Test
    fun `test getCheckedUsers throws an error for an item that doesn't exist`() {
        val category = ImmutableCategory("Wishlist")
        assertFailsWith<IllegalArgumentException>("Item is not in Immutable Category List")
        { category.getCheckedUsers("item") }
    }

    @Test
    fun `test removeItem by a host removes an item from the category list`() {
        val category = ImmutableCategory("Wishlist")
        val host = Attendee(userId = "user123", name = "Luna", role = Role.HOST)
        category.addItem(host, "item")
        category.removeItem(host, "item")
        assertNull(category.items["item"])
    }

    @Test
    fun `test removeItem by an invitee doesn't remove an item from the category list`() {
        val category = ImmutableCategory("Wishlist")
        val host = Attendee(userId = "user123", name = "Luna", role = Role.HOST)
        category.addItem(host, "item")
        val guest = Attendee(userId = "user567", name = "Harbin", role = Role.GUEST)
        assertFailsWith<IllegalArgumentException>("This attendee is not a host or co-host")
        { category.removeItem(guest, "item") }
    }

    @Test
    fun `test uncheckItem unchecks an item`() {
        val category = ImmutableCategory("Wishlist")
        val host = Attendee(userId = "user123", name = "Luna", role = Role.HOST)
        category.addItem(host, "item")
        category.checkItem("Harbin", "item")
        category.uncheckItem("Harbin", "item")
        assertEquals(category.items["item"], mutableSetOf())
    }
}