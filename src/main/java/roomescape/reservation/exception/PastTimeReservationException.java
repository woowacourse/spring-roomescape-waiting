package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.BusinessException;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

public class PastTimeReservationException extends BusinessException {

    public PastTimeReservationException(final ReservationTime time, final LocalDateTime now) {
        super(buildLogMessage(time, now), buildUserMessage());
    }

    private static String buildLogMessage(final ReservationTime time, final LocalDateTime now) {
        return "Attempted to reserve with past time. input=" + time + ", now=" + now;
    }

    private static String buildUserMessage() {
        return "이미 지난 시간에는 예약할 수 없습니다.";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
