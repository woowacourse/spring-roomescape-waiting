package roomescape.member.domain;

public enum MemberRole {
    MEMBER,
    ADMIN,
    ;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
