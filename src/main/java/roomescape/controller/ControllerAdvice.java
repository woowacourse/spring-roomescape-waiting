package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.controller.dto.ErrorMessageResponse;
import roomescape.system.exception.AuthorizationException;
import roomescape.system.exception.RoomescapeException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessageResponse> handleRuntimeException() {
        ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse("서버 내부 에러가 발생하였습니다.");
        return ResponseEntity.internalServerError().body(errorMessageResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessageResponse> handleJsonParseException() {
        ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse("잘못된 요청 형식입니다.");
        return ResponseEntity.internalServerError().body(errorMessageResponse);
    }

    @ExceptionHandler(RoomescapeException.class)

    public ResponseEntity<ErrorMessageResponse> handleIllegalArgumentException(
        RoomescapeException e) {
        ErrorMessageResponse response = new ErrorMessageResponse(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorMessageResponse> handleAuthorizationException(
        AuthorizationException e) {
        ErrorMessageResponse response = new ErrorMessageResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
