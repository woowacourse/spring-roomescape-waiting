package roomescape.reservation.controller.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static roomescape.reservation.controller.response.ReservationErrorCode.NOT_EXISTS_WAITING;
import static roomescape.reservation.controller.response.ReservationErrorCode.NOT_SAME_SLOT;
import static roomescape.reservation.controller.response.ReservationErrorCode.PAST_RESERVATION;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.domain.exception.NotExistsWaitingException;
import roomescape.reservation.domain.exception.NotSameSlotException;
import roomescape.reservation.domain.exception.PastReservationException;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePastReservationException(PastReservationException e) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.fail(PAST_RESERVATION, e.getMessage()));
    }

    @ExceptionHandler(NotSameSlotException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotSameSlotException(NotSameSlotException e) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.fail(NOT_SAME_SLOT, e.getMessage()));
    }

    @ExceptionHandler(NotExistsWaitingException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotExistsWaitingException(NotExistsWaitingException e) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.fail(NOT_EXISTS_WAITING, e.getMessage()));
    }
}
