package roomescape.support.exception.errors;

import lombok.Getter;

@Getter
public enum RoomescapeErrors implements Errors {

    INPUT_FORMAT_ERROR("입력 형식이 올바르지 않습니다. 날짜는 yyyy-MM-dd, 시간은 HH:mm 형식으로 입력해주세요."),
    INPUT_VALIDATION_ERROR("입력 검증 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
    INVALID_GENERATED_KEY("생성 키를 조회할 수 없습니다."),
    REQUIRED_PARAMETER_MISSING("필수 요청 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED("요청한 경로에서 지원하지 않는 HTTP 메서드입니다."),
    ;

    private final String message;

    RoomescapeErrors(String message) {
        this.message = message;
    }

    @Override
    public String getCode() {
        return name();
    }
}
