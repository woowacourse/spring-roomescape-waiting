package roomescape.global.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.auth.exception.InvalidCredentialsException;
import roomescape.auth.exception.InvalidTokenException;
import roomescape.global.dto.response.ErrorResponse;
import roomescape.global.exception.AlreadyEntityException;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.UnauthorizedException;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.exception.MemberNotExistException;
import roomescape.reservation.exception.AssociatedReservationExistsException;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.DuplicateThemeException;
import roomescape.reservation.exception.DuplicateTimeException;
import roomescape.global.exception.InvalidInputException;
import roomescape.reservation.exception.NotCorrectDateTimeException;
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

    @ExceptionHandler(value = {ThemeNotExistException.class, TimeNotExistException.class, MemberNotExistException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEntityNotExist(RuntimeException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = NotCorrectDateTimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotCorrectDateTimeExist(NotCorrectDateTimeException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidToken(InvalidTokenException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(UnauthorizedException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException exception) {
        return generateErrorResponse("접근 권한이 없다.");
    }

    @ExceptionHandler(value = AssociatedReservationExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAssociateReservationExists(AssociatedReservationExistsException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = {DuplicateReservationException.class, DuplicateThemeException.class, DuplicateTimeException.class, DuplicateMemberException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateEntity(RuntimeException exception) {
        return generateErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(value = AlreadyEntityException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAlreadyEntity(AlreadyEntityException exception) {
        exception.printStackTrace();
        return generateErrorResponse("서버 오류 발생. 관리자에게 문의하시오.");
    }

    private ErrorResponse generateErrorResponse(String message) {
        return new ErrorResponse(message);
    }
}
