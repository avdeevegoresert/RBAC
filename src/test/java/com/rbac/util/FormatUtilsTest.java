package com.rbac.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {
    
    @Test
    void testTruncate() {
        assertEquals("Hello", FormatUtils.truncate("Hello", 10));
        assertEquals("Hel...", FormatUtils.truncate("Hello World", 6));
        assertEquals("", FormatUtils.truncate(null, 5));
    }
    
    @Test
    void testPadRight() {
        assertEquals("Hello     ", FormatUtils.padRight("Hello", 10));
        assertEquals("Hello", FormatUtils.padRight("Hello", 3));
    }
    
    @Test
    void testPadLeft() {
        assertEquals("     Hello", FormatUtils.padLeft("Hello", 10));
        assertEquals("Hello", FormatUtils.padLeft("Hello", 3));
    }
    
    @Test
    void testFormatHeader() {
        String header = FormatUtils.formatHeader("Тест");
        assertTrue(header.contains("Тест"));
        assertTrue(header.contains("---"));
    }
    
    @Test
    void testFormatBox() {
        String box = FormatUtils.formatBox("Привет\nМир");
        assertTrue(box.contains("| Привет |"));
        assertTrue(box.contains("| Мир |"));
        assertTrue(box.contains("+"));
    }
}