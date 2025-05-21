package roomescape.reservation.exception;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.BusinessException;
import roomescape.reservation.domain.ReservationDate;

import java.time.LocalDateTime;

public class PastDateReservationException extends BusinessException {

    public PastDateReservationException(final ReservationDate date, final LocalDateTime now) {
        super(buildLogMessage(date, now), buildUserMessage());
    }

    private static String buildLogMessage(final ReservationDate date, final LocalDateTime now) {
        return "Attempted to reserve with past date. input=" + date + ", now=" + now;
    }

    private static String buildUserMessage() {
        return "지난 날짜는 예약할 수 없습니다.";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
