package roomescape.exception.time;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class DuplicatedTimeException extends CustomException {
    public DuplicatedTimeException() {
        super("중복된 예약시간입니다.", HttpStatus.CONFLICT);
    }
}
