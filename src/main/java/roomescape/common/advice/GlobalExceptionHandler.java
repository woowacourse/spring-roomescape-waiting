package roomescape.common.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;
    private final DomainErrorHttpMapper httpMapper;

    @ExceptionHandler(RoomEscapeException.class)
    public ProblemDetail handleDomain(
            RoomEscapeException e,
            Locale locale,
            HttpServletRequest request
    ) {
        HttpStatus status = httpMapper.statusOf(e.code());
        String detail = messageSource.getMessage(e.code().messageKey(), e.args(), locale);
        String title = messageSource.getMessage(e.code().titleKey(), null, locale);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("code", e.code().name());
        return pd;
    }

    public GlobalExceptionHandler(MessageSource messageSource, DomainErrorHttpMapper httpMapper) {
        this.messageSource = messageSource;
        this.httpMapper = httpMapper;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = "요청 본문의 형식이 올바르지 않습니다. 날짜는 uuuu-MM-dd, 시간은 HH:mm 형식으로 입력해 주세요.";
        return invalidInputProblemDetail(message, locale, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = e
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();

        return invalidInputProblemDetail(message, locale, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e,
            Locale locale,
            HttpServletRequest request
    ) {
        String message = String.format(
                "'%s' 값의 형식이 올바르지 않습니다.",
                e.getName()
        );

        return invalidInputProblemDetail(message, locale, request);
    }

    private ProblemDetail invalidInputProblemDetail(
            String message,
            Locale locale,
            HttpServletRequest request
    ) {
        DomainErrorCode code = DomainErrorCode.INVALID_INPUT;
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String detail = messageSource.getMessage(code.messageKey(), new Object[]{message}, locale);
        String title = messageSource.getMessage(code.titleKey(), null, locale);

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(status, detail);
        pb.setTitle(title);
        pb.setInstance(URI.create(request.getRequestURI()));
        pb.setProperty("code", code.name());
        return pb;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnExcepted(
            Exception e,
            Locale locale,
            HttpServletRequest request
    ) {
        log.error("Unexpected error at {}", request.getRequestURI(), e);
        String detail = messageSource.getMessage("error.unexpected", null, locale);
        String title = messageSource.getMessage("error.title.unexpected", null, locale);

        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, detail);
        pb.setTitle(title);
        pb.setInstance(URI.create(request.getRequestURI()));
        return pb;
    }

}
