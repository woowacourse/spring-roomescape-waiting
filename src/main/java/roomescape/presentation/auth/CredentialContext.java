package roomescape.presentation.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;
import roomescape.exception.UnAuthorizedException;

@Component
@RequestScope
public class CredentialContext {
    private MemberRole memberRole;

    public void setCredentialIfNotPresent(MemberRole memberRole) {
        if (this.memberRole != null) {
            throw new IllegalStateException("이미 인증 정보가 존재합니다.");
        }
        this.memberRole = memberRole;
    }

    public void validatePermission(Role requiredRole) {
        if (memberRole == null || !memberRole.hasRoleOf(requiredRole)) {
            throw new UnAuthorizedException();
        }
    }

    public boolean hasCredential() {
        return memberRole != null;
    }

    public long getMemberId() {
        if (memberRole == null) {
            throw new IllegalStateException("인증에 오류가 발생했습니다.");
        }
        return memberRole.getMemberId();
    }
}
