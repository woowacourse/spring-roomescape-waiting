package roomescape.member.domain;

public enum Role {
    ADMIN,
    MEMBER;

    public static Role from(final String value) {
        for (Role role : values()) {
            if (role.name().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 권한입니다.");
    }
}
