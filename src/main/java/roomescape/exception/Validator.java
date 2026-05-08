package roomescape.exception;

public class Validator {
    public static void validateNotNull(Object object)  {
        if (object == null) {
            throw new IllegalArgumentException("null 일 수 없습니다.");
        }
    }
}
