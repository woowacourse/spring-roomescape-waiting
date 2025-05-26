package roomescape.common;

public class CaseFormat {

    public static String convertCamelToSnake(String camel) {
        StringBuilder result = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString().replaceFirst("_", "");
    }
}
