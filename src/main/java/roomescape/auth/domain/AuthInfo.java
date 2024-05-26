package roomescape.auth.domain;

import java.util.Objects;
import roomescape.member.domain.Role;

public class AuthInfo {
    private final Long memberId;
    private final String name;
    private final Role role;

    public AuthInfo(Long memberId, String name, Role role) {
        this.memberId = memberId;
        this.name = name;
        this.role = role;
    }

    public boolean isNotAdmin() {
        return role.isNotAdmin();
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthInfo authInfo = (AuthInfo) o;
        return Objects.equals(memberId, authInfo.memberId) && Objects.equals(name, authInfo.name)
                && role == authInfo.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, name, role);
    }
}
