package roomescape.presentation.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import roomescape.application.auth.dto.TokenPayload;
import roomescape.domain.role.Role;
import roomescape.exception.UnAuthorizedException;

@Component
@RequestScope
public class CredentialContext {
    private TokenPayload payload;

    public void setCredentialIfNotPresent(TokenPayload payload) {
        if (this.payload != null) {
            throw new IllegalStateException("이미 인증 정보가 존재합니다.");
        }
        this.payload = payload;
    }

    public void validatePermission(Role requiredRole) {
        if (payload == null || !payload.hasRoleOf(requiredRole)) {
            throw new UnAuthorizedException();
        }
    }

    public boolean hasCredential() {
        return payload != null;
    }

    public long getMemberId() {
        if (payload == null) {
            throw new IllegalStateException("인증에 오류가 발생했습니다.");
        }
        return payload.memberId();
    }
}
