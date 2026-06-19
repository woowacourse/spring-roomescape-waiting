package roomescape.payment.toss;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 토스 결제 예외 전용 예외 처리. 토스를 아는 web 결합을 이 한 클래스에 국소화해,
 * 전역 핸들러(GlobalExceptionHandler)는 토스를 모르게 둔다.
 *
 * <p>순서 주의: TossPaymentException은 RuntimeException이라, 글로벌 핸들러의
 * handleUnexpected(RuntimeException)가 먼저 가로채면 500이 된다. 이 advice를
 * HIGHEST_PRECEDENCE로 올려 글로벌보다 먼저 매칭되게 한다.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class TossPaymentExceptionAdvice {

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<ProblemDetail> handle(TossPaymentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(e.getStatus().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status).body(problem);
    }
}
