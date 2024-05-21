package roomescape.domain;

public enum Role {
    USER,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
