package Utils;

public class ValidationManagement {

    public static boolean isValueAnInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public static boolean isValueAnIntegerAndGreaterThanZero(String value) {
        if (!isValueAnInteger(value)) return false;

        int number = Integer.parseInt(value);

        return number > 0;
    }

}
