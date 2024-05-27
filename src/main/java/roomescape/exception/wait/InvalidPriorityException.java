package roomescape.exception.wait;

import org.springframework.http.HttpStatus;

import roomescape.exception.CustomException;

public class InvalidPriorityException extends CustomException {
    public InvalidPriorityException() {
        super("잘못된 우선순위입니다", HttpStatus.BAD_REQUEST);
    }
}
