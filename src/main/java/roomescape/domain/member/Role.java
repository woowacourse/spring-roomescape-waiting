package roomescape.domain.member;

public enum Role {
    ADMIN, USER;

    boolean isAdmin() {
        return this == ADMIN;
    }
}
