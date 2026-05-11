package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ReservationAlreadyExistException extends RuntimeException {

    public ReservationAlreadyExistException() {
        super("이미 예약된 시간입니다.");
    }
}
