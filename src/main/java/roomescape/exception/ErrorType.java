package roomescape.exception;

public enum ErrorType {
    INVALID_DATA_TYPE("요청 데이터 형태가 일치하지 않습니다."),
    EMPTY_VALUE_NOT_ALLOWED("필수로 입력해야하는 값이 누락되어 있습니다."),
    INVALID_ID("올바르지 않는 Id 입니다.");

    private final String message;

    ErrorType(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
