package roomescape.service.exception;

import org.springframework.dao.DataAccessException;

public class ReservationExistsException extends DataAccessException {

    public ReservationExistsException() {
        super("해당 데이터를 사용하는 예약이 존재합니다.");
    }
}
