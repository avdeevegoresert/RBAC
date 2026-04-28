package com.rbac.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {
    
    @Test
    void testIsValidUsername() {
        assertTrue(ValidationUtils.isValidUsername("avdeev_egor"));
        assertTrue(ValidationUtils.isValidUsername("simonyan_levon"));
        assertTrue(ValidationUtils.isValidUsername("mamarasulov_a"));
        assertTrue(ValidationUtils.isValidUsername("beletskiy_d"));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("очень_длинный_юзернейм_который_больше_20"));
        assertFalse(ValidationUtils.isValidUsername("user@name"));
        assertFalse(ValidationUtils.isValidUsername(null));
    }
    
    @Test
    void testIsValidEmail() {
        assertTrue(ValidationUtils.isValidEmail("egor@mail.ru"));
        assertTrue(ValidationUtils.isValidEmail("levon@gmail.com"));
        assertTrue(ValidationUtils.isValidEmail("aziz@company.com"));
        assertFalse(ValidationUtils.isValidEmail("egor@mail"));
        assertFalse(ValidationUtils.isValidEmail("egor@.ru"));
        assertFalse(ValidationUtils.isValidEmail("egormail.ru"));
        assertFalse(ValidationUtils.isValidEmail(null));
    }
    
    @Test
    void testIsValidDate() {
        assertTrue(ValidationUtils.isValidDate("2025-12-31"));
        assertTrue(ValidationUtils.isValidDate("2024-02-29"));
        assertFalse(ValidationUtils.isValidDate("2025-13-01"));
        assertFalse(ValidationUtils.isValidDate("2025-01-32"));
        assertFalse(ValidationUtils.isValidDate("2025/12/31"));
        assertFalse(ValidationUtils.isValidDate(null));
    }
    
    @Test
    void testNormalizeString() {
        assertEquals("Авдеев Егор", ValidationUtils.normalizeString("  Авдеев   Егор  "));
        assertEquals("Симонян Левон", ValidationUtils.normalizeString("Симонян Левон"));
        assertNull(ValidationUtils.normalizeString(null));
    }
    
    @Test
    void testRequireNonEmpty() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("Иван", "Имя"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("", "Имя"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("   ", "Имя"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty(null, "Имя"));
    }
    
    @Test
    void testValidateUsername() {
        assertDoesNotThrow(() -> ValidationUtils.validateUsername("avdeev_egor"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateUsername("ab"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateUsername("иван"));
    }
    
    @Test
    void testValidateEmail() {
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("egor@mail.ru"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateEmail("invalid"));
    }
    
    @Test
    void testValidateDate() {
        assertDoesNotThrow(() -> ValidationUtils.validateDate("2025-12-31"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateDate("invalid"));
    }
}