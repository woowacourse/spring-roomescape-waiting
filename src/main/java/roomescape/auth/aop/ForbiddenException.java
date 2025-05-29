package roomescape.auth.aop;

import org.springframework.http.HttpStatus;
import roomescape.common.exception.base.AuthException;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserRole;

import java.util.List;

public class ForbiddenException extends AuthException {

    public ForbiddenException(final UserId userId,
                              final UserRole role,
                              final List<UserRole> requiredRole) {
        super(
                buildLogMessage(userId, role, requiredRole),
                buildUserMessage()
        );
    }

    public ForbiddenException(final UserId userId,
                              final UserRole role,
                              final UserId targetUserId) {
        super(
                buildLogMessage(userId, role, targetUserId),
                buildUserMessage()
        );
    }

    private static String buildLogMessage(final UserId id,
                                          final UserRole role,
                                          final List<UserRole> requiredRoles) {
        final String requiredRoleInfo = String.join(", ",
                requiredRoles.stream()
                        .map(UserRole::name)
                        .toList());

        return String.format(
                "Forbidden access: user(id=%s, role=%s) tried to access resource requiring roles=[%s]",
                id.getValue(), role.name(), requiredRoleInfo);
    }

    private static String buildLogMessage(final UserId id,
                                          final UserRole role,
                                          final UserId targetUserId) {

        return String.format(
                "Forbidden access: user(id=%s, role=%s) tried to access resource belonging to user(id=%s)",
                id.getValue(), role.name(), targetUserId.getValue());
    }

    private static String buildUserMessage() {
        return "권한이 존재하지 않습니다";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
