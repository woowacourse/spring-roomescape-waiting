package roomescape.exception.theme;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class ReservationReferencedThemeException extends CustomException {
    public ReservationReferencedThemeException() {
        super("예약이 존재하는 테마입니다.", HttpStatus.BAD_REQUEST);
    }
}
