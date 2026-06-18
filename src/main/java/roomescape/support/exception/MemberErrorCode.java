package roomescape.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
        "지정한 식별자에 해당하는 멤버를 찾을 수 없습니다.", "요청한 멤버 ID의 유효성 및 DB 존재 여부를 확인하십시오."),
    INVALID_MEMBER(HttpStatus.BAD_REQUEST,
        "멤버 정보가 유효하지 않습니다.", "memberId 필드 포함 여부 및 데이터 형식을 확인하십시오.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String action;

    MemberErrorCode(HttpStatus httpStatus, String message, String action) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.action = action;
    }

    @Override
    public String getCode() {
        return name();
    }
}
