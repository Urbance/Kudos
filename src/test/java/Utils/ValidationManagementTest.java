package Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationManagementTest {

    @Test
    void isValueAnInteger_charAIsNotAnInteger() {
        assertFalse(ValidationManagement.isValueAnInteger("A"));
    }

    @Test
    void isValueAnInteger_numberThreeIsAnInteger() {
        assertTrue(ValidationManagement.isValueAnInteger("3"));
    }

    @Test
    void isValueAnInteger_numberGreaterThanAnIntegerEqualsFalse() {
        assertFalse(ValidationManagement.isValueAnInteger("2147483648"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_negativeTenIsAnIntegerAndEqualsFalse() {
        assertFalse(ValidationManagement.isValueAnIntegerAndGreaterThanZero("-10"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_charAEqualsFalse() {
        assertFalse(ValidationManagement.isValueAnIntegerAndGreaterThanZero("A"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_int0EqualsFalse() {
        assertFalse(ValidationManagement.isValueAnIntegerAndGreaterThanZero("0"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_int1EqualsTrue() {
        assertTrue(ValidationManagement.isValueAnIntegerAndGreaterThanZero("1"));
    }

}