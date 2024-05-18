package roomescape.member.domain;

public enum Role {
    ADMIN,
    USER,
    ;

    public boolean isNotAdmin() {
        return this != ADMIN;
    }
}
