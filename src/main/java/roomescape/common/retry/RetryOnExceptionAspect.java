package roomescape.common.retry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // @Transactional 보다 먼저 감싸져야 하기 때문에
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
                    throw e;
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
