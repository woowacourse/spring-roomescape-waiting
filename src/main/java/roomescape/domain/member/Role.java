package roomescape.domain.member;

public enum Role {
    ADMIN, GUEST;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isGuest() {
        return this == GUEST;
    }
}
