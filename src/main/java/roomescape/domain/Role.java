package roomescape.domain;

public enum Role {

    ADMIN,
    CUSTOMER,
    ;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
