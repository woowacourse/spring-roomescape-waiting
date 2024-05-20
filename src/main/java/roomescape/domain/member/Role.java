package roomescape.domain.member;

public enum Role {
    ADMIN, MEMBER, GUEST;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
