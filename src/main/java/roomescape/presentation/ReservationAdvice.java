package roomescape.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.application.auth.dto.TokenPayload;
import roomescape.exception.reservation.AlreadyBookedException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.WaitingListExceededException;
import roomescape.presentation.auth.CredentialContext;

@RestControllerAdvice
public class ReservationAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ReservationAdvice.class);

    private final CredentialContext context;

    public ReservationAdvice(CredentialContext context) {
        this.context = context;
    }

    @ExceptionHandler(WaitingListExceededException.class)
    public ProblemDetail handleWaitingListExceededException(WaitingListExceededException exception) {
        TokenPayload payload = context.getPayload();
        String message = "Member %s (#%d) exceeded waiting limit on Reservation #%d"
                .formatted(payload.name(), payload.memberId(), exception.getReservationId());
        logger.error(message, exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AlreadyBookedException.class)
    public ProblemDetail handleAlreadyBookedException(AlreadyBookedException exception) {
        TokenPayload payload = context.getPayload();
        String message = "Member %s (#%d) failed to book Reservation on %s at Time #%d with Theme #%d: Already booked"
                .formatted(payload.name(), payload.memberId(),
                        exception.getDate(), exception.getTimeId(), exception.getThemeId());
        logger.error(message, exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DuplicatedReservationException.class)
    public ProblemDetail handleDuplicatedReservationException(DuplicatedReservationException exception) {
        TokenPayload payload = context.getPayload();
        String message = ("Member %s (#%d) failed to book Reservation"
                + "(theme #%d, date %s, time #%d: Duplicated booking or waiting")
                .formatted(
                        payload.name(), payload.memberId(),
                        exception.getThemeId(), exception.getDate(), exception.getTimeId()
                );
        logger.error(message, exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
