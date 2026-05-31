package roomescape.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.domain.exception.RoomEscapeException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final DomainErrorHttpMapper httpMapper;

    public GlobalExceptionHandler(MessageSource messageSource, DomainErrorHttpMapper httpMapper) {
        this.messageSource = messageSource;
        this.httpMapper = httpMapper;
    }

    @ExceptionHandler(RoomEscapeException.class)
    public ProblemDetail handleDomain(RoomEscapeException e, Locale locale, HttpServletRequest request) {
        log.warn("[Domain Error] {} args={}", e.code(), Arrays.toString(e.args()), e);
        HttpStatus status = httpMapper.statusOf(e.code());
        String messageKey = "error." + e.code().name().toLowerCase().replace('_', '-');
        String titleKey = "error.title." + status.name().toLowerCase().replace('_', '-');
        String detail = messageSource.getMessage(messageKey, e.args(), locale);
        String title = messageSource.getMessage(titleKey, null, locale);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("code", e.code().name());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception e, Locale locale, HttpServletRequest request) {
        log.error("[Internal Server Error]", e);
        String detail = messageSource.getMessage("error.unexpected", null, locale);
        String title = messageSource.getMessage("error.title.unexpected", null, locale);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }
}
