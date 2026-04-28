package com.rbac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {
    
    @Test
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }
    
    @Test
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2025-01-01", "2025-12-31"));
        assertFalse(DateUtils.isBefore("2025-12-31", "2025-01-01"));
        assertFalse(DateUtils.isBefore("2025-01-01", "2025-01-01"));
    }
    
    @Test
    void testIsAfter() {
        assertTrue(DateUtils.isAfter("2025-12-31", "2025-01-01"));
        assertFalse(DateUtils.isAfter("2025-01-01", "2025-12-31"));
        assertFalse(DateUtils.isAfter("2025-01-01", "2025-01-01"));
    }
    
    @Test
    void testAddDays() {
        assertEquals("2025-01-10", DateUtils.addDays("2025-01-05", 5));
        assertEquals("2024-12-31", DateUtils.addDays("2025-01-01", -1));
        assertNull(DateUtils.addDays(null, 5));
    }
    
    @Test
    void testFormatRelativeTime() {
        String result = DateUtils.formatRelativeTime(DateUtils.getCurrentDate());
        assertEquals("сегодня", result);
    }
    
    @Test
    void testIsValidDateFormat() {
        assertTrue(DateUtils.isValidDateFormat("2025-12-31"));
        assertFalse(DateUtils.isValidDateFormat("31-12-2025"));
        assertFalse(DateUtils.isValidDateFormat("2025-13-31"));
        assertFalse(DateUtils.isValidDateFormat(null));
    }
}