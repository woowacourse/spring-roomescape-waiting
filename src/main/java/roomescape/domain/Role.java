package roomescape.domain;

public enum Role {

    ADMIN,
    NORMAL;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
