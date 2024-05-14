package roomescape.domain;

public enum Role {
    BASIC,
    ADMIN
    ;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
