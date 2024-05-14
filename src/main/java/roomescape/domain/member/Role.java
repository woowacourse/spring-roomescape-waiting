package roomescape.domain.member;

public enum Role {
    ADMIN, GUEST;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
