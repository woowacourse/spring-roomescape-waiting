package roomescape.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import roomescape.common.exception.base.AuthException;
import roomescape.common.exception.base.BusinessException;
import roomescape.common.validate.InvalidInputException;

import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(final BusinessException ex, final WebRequest request) {
        log.warn("BusinessException ({}): {} at {}", ex.getClass().getSimpleName(), ex.getMessage(), getPath(request));
        return createProblemDetail(
                ex.getHttpStatus(),
                "비즈니스 로직 오류",
                ex.getUserMessage().orElse("요청을 처리할 수 없습니다."), request);
    }

    @ExceptionHandler(AuthException.class)
    public ProblemDetail handleAuthException(final AuthException ex, final WebRequest request) {
        log.warn("AuthException ({}): {} at {}", ex.getClass().getSimpleName(), ex.getMessage(), getPath(request));
        return createProblemDetail(
                ex.getHttpStatus(),
                "인증/인가 로직 오류",
                ex.getUserMessage().orElse("인증/인가를 실패했습니다."), request);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ProblemDetail handleDateTimeParseException(final DateTimeParseException ex, final WebRequest request) {
        log.warn("DateTimeParseException: {} at {}", ex.getMessage(), getPath(request));
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "날짜/시간 파싱 오류",
                "날짜나 시간 형식이 잘못되었습니다.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(final IllegalArgumentException ex, final WebRequest request) {
        log.error("IllegalArgumentException: {} at {}", ex.getMessage(), getPath(request), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "잘못된 내부 인자",
                "서버 내부 오류가 발생했습니다.", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(final IllegalStateException ex, final WebRequest request) {
        log.error("IllegalStateException: {} at {}", ex.getMessage(), getPath(request), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "잘못된 서버 상태",
                "서버 내부 오류가 발생했습니다.", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleJsonParseError(final HttpMessageNotReadableException ex, final WebRequest request) {
        final Optional<InvalidInputException> cause =
                Stream.iterate(ex.getCause(), Objects::nonNull, Throwable::getCause)
                        .filter(InvalidInputException.class::isInstance)
                        .map(InvalidInputException.class::cast)
                        .findFirst();

        if (cause.isPresent()) {
            log.warn("Validation during deserialization: {}", cause.get().getMessage());
            return handleBusinessException(cause.get(), request);
        }

        log.warn("Json parse error at {}: {}", getPath(request), ex.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "요청 본문 파싱 오류",
                "요청 본문이 잘못되었습니다.", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(final Exception ex, final WebRequest request) {
        log.error("Unhandled Exception: {} at {}", ex.getMessage(), getPath(request), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류",
                "예기치 못한 문제가 발생했습니다. 관리자에게 문의하세요.", request);
    }

    private ProblemDetail createProblemDetail(final HttpStatus status,
                                              final String title,
                                              final String detail,
                                              final WebRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setInstance(getInstanceURI(request));
        return problemDetail;
    }

    private URI getInstanceURI(final WebRequest request) {
        return URI.create(getPath(request));
    }

    private String getPath(final WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
