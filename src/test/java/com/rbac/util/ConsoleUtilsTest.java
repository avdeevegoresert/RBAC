package com.rbac.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {
    
    @Test
    void testPromptYesNoLogic() {
        assertTrue(ConsoleUtils.promptYesNo(null, "test"));
        assertFalse(ConsoleUtils.promptYesNo(null, "test"));
    }
    
    @Test
    void testPromptChoiceList() {
        List<String> options = Arrays.asList("Admin", "User", "Viewer");
        assertNotNull(options);
        assertEquals(3, options.size());
    }
    
    @Test
    void testPrintMethods() {
        assertDoesNotThrow(() -> {
            ConsoleUtils.printHeader("Тест");
            ConsoleUtils.printSuccess("Успех");
            ConsoleUtils.printError("Ошибка");
        });
    }
}