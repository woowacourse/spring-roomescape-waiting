package roomescape.common.exception.message;

public enum GlobalExceptionMessage {
    NULL_VALUE("널 값은 저장될 수 없습니다."),
    INVALID_INPUT_VALUE("입력 값이 올바르지 않습니다.");

    private final String message;

    GlobalExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
