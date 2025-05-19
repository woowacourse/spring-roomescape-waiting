package roomescape.auth.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import roomescape.auth.session.Session;
import roomescape.auth.session.annotation.UserSession;
import roomescape.auth.session.util.UserSessionExtractor;
import roomescape.common.servlet.ServletRequestHolder;
import roomescape.user.domain.UserRole;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class RequiredRoleAspect {

    private final UserSessionExtractor userSessionExtractor;

    @Around("@annotation(requiredRoles)")
    public Object checkMethodLevel(final ProceedingJoinPoint joinPoint,
                                   final RequiredRoles requiredRoles) throws Throwable {
        return check(joinPoint, requiredRoles.value());
    }

    @Around("@within(requiredRoles)")
    public Object checkClassLevel(final ProceedingJoinPoint joinPoint,
                                  final RequiredRoles requiredRoles) throws Throwable {
        return check(joinPoint, requiredRoles.value());
    }

    private Object check(final ProceedingJoinPoint joinPoint,
                         final UserRole[] requiredRoles) throws Throwable {
        final Session session = extractUserSession(joinPoint);
        validateAuthorization(session, requiredRoles);
        return joinPoint.proceed();
    }

    private void validateAuthorization(final Session session, final UserRole[] requiredRoles) {
        final boolean authorized = Arrays.stream(requiredRoles)
                .anyMatch(session.role()::includes);

        if (!authorized) {
            throw new ForbiddenException(session.id(), session.role(), List.of(requiredRoles));
        }
    }

    private Session extractUserSession(final ProceedingJoinPoint joinPoint) {
        return findAnnotatedUserSession(joinPoint)
                .orElseGet(this::extractUserSessionFromRequest);
    }

    private Optional<Session> findAnnotatedUserSession(final ProceedingJoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Object[] args = joinPoint.getArgs();
        final Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
            if (isAnnotatedUserSession(args[i], paramAnnotations[i])) {
                return Optional.of((Session) args[i]);
            }
        }
        return Optional.empty();
    }

    private boolean isAnnotatedUserSession(final Object arg, final Annotation[] annotations) {
        return arg instanceof Session &&
                Arrays.stream(annotations)
                        .anyMatch(annotation -> annotation.annotationType().equals(UserSession.class));
    }

    private Session extractUserSessionFromRequest() {
        return userSessionExtractor.execute(
                ServletRequestHolder.getRequest());
    }
}
