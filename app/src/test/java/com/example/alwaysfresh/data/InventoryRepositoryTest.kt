package com.example.alwaysfresh.data

import com.example.alwaysfresh.model.FreshStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InventoryRepositoryTest {

    // Pinned "today" so every test is deterministic regardless of the machine clock.
    private val today: LocalDate = LocalDate.of(2026, 4, 27)

    private fun classify(date: String) =
        InventoryRepository.classifyItem(date, today)

    // ── Past dates ────────────────────────────────────────────────────────
    @Test
    fun `expired when date is yesterday`() {
        assertEquals(FreshStatus.EXPIRED, classify("2026-04-26"))
    }

    @Test
    fun `expired when date is months in the past`() {
        assertEquals(FreshStatus.EXPIRED, classify("2025-01-01"))
    }

    // ── Today and the seven-day window ────────────────────────────────────
    @Test
    fun `expiring soon when date is today`() {
        assertEquals(FreshStatus.EXPIRING_SOON, classify("2026-04-27"))
    }

    @Test
    fun `expiring soon when date is one day away`() {
        assertEquals(FreshStatus.EXPIRING_SOON, classify("2026-04-28"))
    }

    @Test
    fun `expiring soon at the seven-day boundary`() {
        assertEquals(FreshStatus.EXPIRING_SOON, classify("2026-05-04"))
    }

    @Test
    fun `fresh just past the seven-day boundary`() {
        assertEquals(FreshStatus.FRESH, classify("2026-05-05"))
    }

    @Test
    fun `fresh when date is far in the future`() {
        assertEquals(FreshStatus.FRESH, classify("2027-01-01"))
    }

    // ── Year boundary ─────────────────────────────────────────────────────
    @Test
    fun `expiring soon across a year boundary within seven days`() {
        val newYearsEveMinus = LocalDate.of(2026, 12, 30)
        assertEquals(
            FreshStatus.EXPIRING_SOON,
            InventoryRepository.classifyItem("2027-01-05", newYearsEveMinus)
        )
    }

    // ── Defensive parsing — malformed input must not crash ────────────────
    @Test
    fun `expired when date string is malformed`() {
        assertEquals(FreshStatus.EXPIRED, classify("not-a-date"))
    }

    @Test
    fun `expired when date string is empty`() {
        assertEquals(FreshStatus.EXPIRED, classify(""))
    }

    @Test
    fun `expired when month is out of range`() {
        assertEquals(FreshStatus.EXPIRED, classify("2026-13-01"))
    }

    @Test
    fun `expired when day is out of range`() {
        assertEquals(FreshStatus.EXPIRED, classify("2026-02-30"))
    }
}
