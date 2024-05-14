package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = Controller.class)
public class ExceptionPageController {

    @ExceptionHandler(InValidRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String InValidRoleExceptionHandler(InValidRoleException exception) {
        return exception.getMessage();
    }
}
