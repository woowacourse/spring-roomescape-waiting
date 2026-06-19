package roomescape.feature.time.error.type;

import org.springframework.http.HttpStatus;
import roomescape.global.error.type.ErrorType;

public enum TimeErrorType implements ErrorType {
    FIELD_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "조회할 자원이 존재하지 않습니다."),
    ALREADY_EXIST_TIME(HttpStatus.CONFLICT, "이미 등록된 예약 시간입니다."),
    TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 시간을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    TimeErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String code() {
        return name();
    }
}
