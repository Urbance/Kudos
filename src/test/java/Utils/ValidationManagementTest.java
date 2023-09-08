package Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationManagementTest {

    private ValidationManagement validationManagement = new ValidationManagement();

    @Test
    void isValueAnInteger_charAIsNotAnInteger() {
        assertFalse(validationManagement.isValueAnInteger("A"));
    }

    @Test
    void isValueAnInteger_numberThreeIsAnInteger() {
        assertTrue(validationManagement.isValueAnInteger("3"));
    }

    @Test
    void isValueAnInteger_numberGreaterThanAnIntegerEqualsFalse() {
        assertFalse(validationManagement.isValueAnInteger("2147483648"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_negativeTenIsAnIntegerAndEqualsFalse() {
        assertFalse(validationManagement.isValueAnIntegerAndGreaterThanZero("-10"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_charAEqualsFalse() {
        assertFalse(validationManagement.isValueAnIntegerAndGreaterThanZero("A"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_int0EqualsFalse() {
        assertFalse(validationManagement.isValueAnIntegerAndGreaterThanZero("0"));
    }

    @Test
    void isValueAnIntegerAndGreaterThanZero_int1EqualsTrue() {
        assertTrue(validationManagement.isValueAnIntegerAndGreaterThanZero("1"));
    }

}