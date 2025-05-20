package roomescape.member.role;

import roomescape.member.resolver.UnauthenticatedException;

public enum Role {

    ADMIN,
    MEMBER;
    
    public void validateAdmin() {
        if (this == ADMIN) {
            return;
        }
        throw new UnauthenticatedException("[ERROR] 권한이 없습니다.");
    }
}
