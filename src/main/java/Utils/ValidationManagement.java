package Utils;

public class ValidationManagement {

    public boolean isValueAnInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public boolean isValueAnIntegerAndGreaterThanZero(String value) {
        if (!isValueAnInteger(value)) return false;

        int number = Integer.parseInt(value);

        return number > 0;
    }

}
