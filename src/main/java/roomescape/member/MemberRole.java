package roomescape.member;

public enum MemberRole {
    ADMIN, MEMBER;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
