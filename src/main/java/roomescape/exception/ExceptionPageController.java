package roomescape.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionPageController {

    @ExceptionHandler(NoAdminPrivilegeException.class)
    public String noAdminPrivilegeExHandler(HttpServletResponse response) {
        int statusCode = NoAdminPrivilegeException.STATUS_CODE;
        response.setStatus(statusCode);

        return "error/" + statusCode;
    }

    @ExceptionHandler(UnauthenticatedUserException.class)
    public String unauthenticatedUserExHandler(HttpServletResponse response) {
        int statusCode = UnauthenticatedUserException.STATUS_CODE;
        response.setStatus(statusCode);

        return "error/" + statusCode;
    }
}
