package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;

import roomescape.global.error.BusinessException;
import roomescape.global.error.ErrorCode;

public class ReservationNotFoundException extends BusinessException {
    public ReservationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.RESERVATION_NOT_FOUND, "예약을 찾을 수 없습니다. id=" + id);
    }
}
