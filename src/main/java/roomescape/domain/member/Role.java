package roomescape.domain.member;

public enum Role {
    ADMIN,
    MEMBER;

    public static Role getRole(String role) {
        if (ADMIN.name().equals(role)) {
            return ADMIN;
        }
        if (MEMBER.name().equals(role)) {
            return MEMBER;
        }
        throw new IllegalArgumentException("회원의 권한이 존재하지 않습니다.");
    }
}
