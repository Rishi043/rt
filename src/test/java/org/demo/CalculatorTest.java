package org.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Creating Class with name CalculatorTest to test Calculator methods
class CalculatorTest {

    // Test case for add method
    @Test
    void testAdd() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3));
    }

    // Test case for subtract method
    @Test
    void testSubtract() {
        Calculator calc = new Calculator();
        assertEquals(1, calc.subtract(3, 2));
    }
}
