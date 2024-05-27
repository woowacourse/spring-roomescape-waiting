package roomescape.domain.member;

public enum Role {
    ADMIN,
    MEMBER,
    ;

    public static Role from(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("해당하는 역할이 없습니다.");
        }
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
