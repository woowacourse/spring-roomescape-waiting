package roomescape.holiday.exception;

import org.springframework.http.HttpStatus;

import roomescape.error.BusinessException;
import roomescape.error.ErrorCode;

public class HolidayNotFoundException extends BusinessException {
    public HolidayNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.HOLIDAY_NOT_FOUND, "휴일 정보를 찾을 수 없습니다. id=" + id);
    }
}
