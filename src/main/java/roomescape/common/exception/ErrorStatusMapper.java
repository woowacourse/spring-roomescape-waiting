package roomescape.common.exception;

import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class ErrorStatusMapper {

    public static final Map<ErrorCode, HttpStatus> statusByErrorCode = new EnumMap<>(ErrorCode.class);

    static {
        statusByErrorCode.put(ErrorCode.INVALID_RESERVATION_ID, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_RESERVATION_NAME, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_RESERVATION_DATE, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_RESERVATION_TIME, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_RESERVATION_TIME_ID, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.RESERVATION_ALREADY_HAS_ID, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.RESERVATION_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.RESERVATION_TIME_ALREADY_HAS_ID, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.RESERVATION_NOT_FOUND, HttpStatus.NOT_FOUND);
        statusByErrorCode.put(ErrorCode.RESERVATION_TIME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.RESERVATION_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        statusByErrorCode.put(ErrorCode.RESERVATION_TIME_NOT_FOUND, HttpStatus.NOT_FOUND);
        statusByErrorCode.put(ErrorCode.RESERVATION_TIME_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        statusByErrorCode.put(ErrorCode.RESERVATION_TIME_HAS_RESERVATION, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_THEME_ID, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_THEME_NAME, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_THEME_DESCRIPTION, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_THEME_THUMBNAIL, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.INVALID_THEME, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.THEME_ALREADY_HAS_ID, HttpStatus.BAD_REQUEST);
        statusByErrorCode.put(ErrorCode.THEME_NOT_FOUND, HttpStatus.NOT_FOUND);
        statusByErrorCode.put(ErrorCode.THEME_CREATE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        statusByErrorCode.put(ErrorCode.THEME_HAS_RESERVATION, HttpStatus.BAD_REQUEST);
    }

    public static HttpStatus map(ErrorCode errorCode) {
        return Optional.ofNullable(statusByErrorCode.get(errorCode))
                .orElseThrow(() -> new IllegalStateException("매핑되지 않은 에러 코드입니다: " + errorCode));
    }
}
