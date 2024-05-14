package roomescape.global.exception.error;

public enum ErrorType {

    // 400 Bad Request
    REQUEST_DATA_BLANK("요청 데이터에 유효하지 않은 값(null OR 공백)이 포함되어있습니다."),
    INVALID_REQUEST_DATA_TYPE("요청 데이터 형식이 올바르지 않습니다."),
    INVALID_MEMBER_NAME("회원(Member)의 이름은 1자 이상 10자 이하여야 합니다."),
    INVALID_MEMBER_PASSWORD("회원(Member)의 패스워드는은 8자 이상 16자 이하여야 합니다."),
    INVALID_MEMBER_EMAIL("회원(Member)의 이메일(Email) 형식이 잘못되었습니다."),

    // 401 Unauthorized
    EXPIRED_TOKEN("만료된 JWT 토큰입니다."),
    UNSUPPORTED_TOKEN("지원하지 않는 JWT 토큰입니다."),
    MALFORMED_TOKEN("형식이 맞지 않는 JWT 토큰입니다."),
    INVALID_SIGNATURE_TOKEN("잘못된 JWT 토큰 Signature 입니다."),
    ILLEGAL_TOKEN("JWT 토큰의 Claim 이 비어있습니다."),
    INVALID_TOKEN("JWT 토큰이 존재하지 않거나 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 RefreshToken 입니다."),

    // 403 Forbidden
    PERMISSION_DOES_NOT_EXIST("접근 권한이 존재하지 않습니다."),

    // 404 Not Found
    MEMBER_NOT_FOUND("회원(Member) 정보가 존재하지 않습니다."),
    RESERVATION_NOT_FOUND("예약(Reservation) 정보가 존재하지 않습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED("지원하지 않는 HTTP Method 입니다."),

    // 409 Conflict
    TIME_IS_USED_CONFLICT("삭제할 수 없는 시간대입니다."),
    TIME_DUPLICATED("이미 해당 시간이 존재합니다."),
    RESERVATION_DUPLICATED("해당 시간에 이미 예약이 존재합니다."),
    RESERVATION_PERIOD_IN_PAST("이미 지난 시간대는 예약할 수 없습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR("서버 내부에서 에러가 발생하였습니다."),
    ;

    private final String description;

    ErrorType(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
