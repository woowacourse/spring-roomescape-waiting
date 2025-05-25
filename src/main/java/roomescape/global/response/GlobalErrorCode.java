package roomescape.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    NO_ELEMENTS("GF001", "리소스가 존재하지 않습니다."),
    WRONG_ARGUMENT("GF002", "잘못된 인자입니다."),
    ROOMESCAPE_SERVER_ERROR("GF003", "서버 오류입니다."),
    IN_ALREADY_EXCEPTION("GF004", "이미 사용 중입니다."),
    ;

    private final String value;
    private final String message;
}
