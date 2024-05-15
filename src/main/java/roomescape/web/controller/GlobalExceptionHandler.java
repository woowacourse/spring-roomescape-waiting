package roomescape.web.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class GlobalExceptionHandler {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    private ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.log(Level.SEVERE, e.getMessage());
        String errorMessage = "요청 본문을 읽을 수 없습니다. 요청 형식을 확인해 주세요.";
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    private ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            logger.log(Level.SEVERE, "[IllegalArgumentException] " + cause.getMessage());
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    private ProblemDetail handleGeneralException(Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        String errorMessage = "시스템에서 오류가 발생했습니다. 관리자에게 문의해주세요.";
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}
