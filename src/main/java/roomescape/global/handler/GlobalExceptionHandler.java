package roomescape.global.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.dto.response.ErrorResponse;
import roomescape.reservation.exception.AssociatedReservationExistsException;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.DuplicateThemeException;
import roomescape.reservation.exception.DuplicateTimeException;
import roomescape.global.exception.InvalidInputException;
import roomescape.reservation.exception.ThemeNotExistException;
import roomescape.reservation.exception.TimeNotExistException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception exception) {
        exception.printStackTrace();
        return generateErrorResponse("예상하지 못한 예외가 발생했다. 자세한 사항은 관리자에게 문의하라.");
    }

    @ExceptionHandler(value = InvalidInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidInput(InvalidInputException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = {ThemeNotExistException.class, TimeNotExistException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEntityNotExist(RuntimeException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = AssociatedReservationExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAssociateReservationExists(AssociatedReservationExistsException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = {DuplicateReservationException.class, DuplicateThemeException.class, DuplicateTimeException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateEntity(RuntimeException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    private ErrorResponse generateErrorResponse(String message) {
        return new ErrorResponse(message);
    }
}
