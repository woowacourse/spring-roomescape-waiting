package roomescape.auth.aop;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.AuthException;
import roomescape.user.domain.UserRole;

import java.util.List;

public class ForbiddenException extends AuthException {

    public ForbiddenException(final Long userId,
                              final UserRole role,
                              final List<UserRole> requiredRole) {
        super(
                buildLogMessage(userId, role, requiredRole),
                buildUserMessage()
        );
    }

    private static String buildLogMessage(final Long userId,
                                          final UserRole role,
                                          final List<UserRole> requiredRoles) {
        final String requiredRolesStr = String.join(", ",
                requiredRoles.stream()
                        .map(UserRole::name)
                        .toList());

        return String.format(
                "Forbidden access: user(userId=%s, role=%s) tried to access resource requiring roles=[%s]",
                userId, role.name(), requiredRolesStr);
    }

    private static String buildUserMessage() {
        return "권한이 존재하지 않습니다";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
