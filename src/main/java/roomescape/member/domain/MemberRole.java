package roomescape.member.domain;

public enum MemberRole {
    ADMIN,
    USER,
    ;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
