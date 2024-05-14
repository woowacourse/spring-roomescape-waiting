package roomescape.auth.domain;

import java.util.Objects;
import roomescape.member.domain.MemberRole;

public class AuthInfo {
    private final Long memberId;
    private final String name;
    private final MemberRole memberRole;

    public AuthInfo(final Long memberId, final String name, final MemberRole memberRole) {
        this.memberId = memberId;
        this.name = name;
        this.memberRole = memberRole;
    }

    public boolean isNotAdmin() {
        return this.memberRole.isNotAdmin();
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthInfo authInfo = (AuthInfo) o;
        return Objects.equals(memberId, authInfo.memberId) && Objects.equals(name, authInfo.name)
                && memberRole == authInfo.memberRole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, name, memberRole);
    }
}
