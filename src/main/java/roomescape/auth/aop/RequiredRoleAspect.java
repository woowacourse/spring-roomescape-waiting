package roomescape.auth.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import roomescape.auth.session.UserSession;
import roomescape.auth.session.UserSessionExtractor;
import roomescape.auth.session.annotation.SignInUser;
import roomescape.common.servlet.ServletRequestHolder;
import roomescape.user.domain.UserRole;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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
        final UserSession userSession = extractUserSession(joinPoint);
        validateAuthorization(userSession, requiredRoles);
        return joinPoint.proceed();
    }

    private void validateAuthorization(final UserSession userSession, final UserRole[] requiredRoles) {
        final boolean unauthorized = Arrays.stream(requiredRoles)
                .noneMatch(userSession.role()::includes);

        if (unauthorized) {
            throw new ForbiddenException(userSession.id(), userSession.role(), List.of(requiredRoles));
        }
    }

    private UserSession extractUserSession(final ProceedingJoinPoint joinPoint) {
        return findAnnotatedUserSession(joinPoint)
                .orElseGet(this::extractUserSessionFromRequest);
    }

    private Optional<UserSession> findAnnotatedUserSession(final ProceedingJoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Object[] args = joinPoint.getArgs();
        final Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

        return IntStream.range(0, Math.min(args.length, paramAnnotations.length))
                .filter(i -> isAnnotatedUserSession(args[i], paramAnnotations[i]))
                .mapToObj(i -> (UserSession) args[i])
                .findAny();
    }

    private boolean isAnnotatedUserSession(final Object arg, final Annotation[] annotations) {
        return arg instanceof UserSession &&
                Arrays.stream(annotations)
                        .anyMatch(annotation -> annotation.annotationType().equals(SignInUser.class));
    }

    private UserSession extractUserSessionFromRequest() {
        return userSessionExtractor.execute(
                ServletRequestHolder.getRequest());
    }
}
