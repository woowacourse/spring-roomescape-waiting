package roomescape.common.retry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import roomescape.common.exception.DomainException;

import static roomescape.reservation.exception.ReservationErrorCode.TOO_MANY_REQUESTS_FOR_RESERVATION;

/**
 * {@link RetryOnException}이 선언된 메서드를 감싸고, 지정된 예외가 발생했을 때 메서드 실행을 재시도하는 Aspect.
 *
 * <p>초기 실행을 포함해 최대 {@code maxRetries + 1}번 시도하며, {@link RetryOnException#retryOn()}에
 * 포함되지 않은 예외는 재시도하지 않고 그대로 전파한다. 모든 재시도가 실패하면 클라이언트에게 재시도를 안내하기 위해
 * {@link DomainException}을 발생시킨다.</p>
 *
 * <p>{@code @Order(Ordered.HIGHEST_PRECEDENCE)}: 각 재시도가 독립된 트랜잭션에서 실행될 수 있도록 {@code @Transactional}보다 높은 우선순위로 적용된다.</p>
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RetryOnExceptionAspect {

    @Around("@annotation(roomescape.common.retry.RetryOnException)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        RetryOnException retryOnException = getRetryOnException(joinPoint);
        int maxRetries = retryOnException.maxRetries();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (!isRetryable(e, retryOnException.retryOn())) {
                    throw e;
                }
                if (attempt == maxRetries) {
                    throw new DomainException(TOO_MANY_REQUESTS_FOR_RESERVATION);
                }
            }
        }

        return joinPoint.proceed();
    }

    private RetryOnException getRetryOnException(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(RetryOnException.class);
    }

    private boolean isRetryable(Throwable exception, Class<? extends Throwable>[] retryableExceptions) {
        for (Class<? extends Throwable> retryableException : retryableExceptions) {
            if (retryableException.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }
        return false;
    }
}
