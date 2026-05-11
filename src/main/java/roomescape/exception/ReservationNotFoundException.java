package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Long id) {
        super(id + "번 예약을 찾을 수 없습니다.");
    }
}
