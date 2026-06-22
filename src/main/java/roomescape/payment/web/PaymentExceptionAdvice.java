package roomescape.payment.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.payment.exception.OutboundRateLimitException;
import roomescape.payment.exception.PaymentGatewayUnreachableException;

/**
 * 결제 게이트웨이로의 나가는 호출이 완료되지 못한 경우를 처리한다(연결 실패, 자체 한도 초과 거부).
 * 둘 다 '결제 실패'가 아니라 '지금은 못 보냄 → 잠시 후 다시 시도'를 안내한다.
 * 전역 handleUnexpected(RuntimeException)가 500으로 가로채지 않도록 우선순위를 올린다.
 * (read timeout '모름'은 예외가 아니라 PaymentService에서 ConfirmOutcome.NEEDS_CHECK로 처리되므로 여기 없다.)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PaymentExceptionAdvice {

    @ExceptionHandler(PaymentGatewayUnreachableException.class)
    public ResponseEntity<ProblemDetail> handleUnreachable(PaymentGatewayUnreachableException e,
                                                           HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "결제 서버에 연결하지 못했습니다. 결제가 진행되지 않았으니 다시 시도해 주세요.");
        problem.setTitle("결제 서버 연결 실패");
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(OutboundRateLimitException.class)
    public ResponseEntity<ProblemDetail> handleOutboundRateLimited(OutboundRateLimitException e,
                                                                   HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "결제 요청이 일시적으로 많아 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        problem.setTitle("결제 호출 일시 제한");
        problem.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }
}
