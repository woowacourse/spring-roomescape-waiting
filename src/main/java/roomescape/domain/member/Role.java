package roomescape.domain.member;

public enum Role {
    ADMIN, USER;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
