package roomescape.domain;

public enum Role {

    ADMIN,
    USER;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
