package roomescape.dto.request.exception;

public class InputValidator {

    public static void validateNotNull(Object... inputs) {
        for (Object input : inputs) {
            if (input == null) {
                throw new InputNotAllowedCustomException("null은 입력할 수 없습니다.");
            }
        }
    }

    public static void validateNotBlank(String... inputs) {
        for (String input : inputs) {
            if (input.isBlank()) {
                throw new InputNotAllowedCustomException("빈 문자열은 입력할 수 없습니다.");
            }
        }
    }
}
