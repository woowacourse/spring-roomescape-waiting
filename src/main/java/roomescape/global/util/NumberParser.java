package roomescape.global.util;

public class NumberParser {

    public static Long parseToLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(value + "는 숫자로 변환할 수 없는 값입니다.", e);
        }
    }
}
