package roomescape.member.domain;

public enum Role {

    ADMIN,
    USER;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
