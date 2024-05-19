package roomescape.domain.member;

public enum Role {
    BASIC,
    ADMIN
    ;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
