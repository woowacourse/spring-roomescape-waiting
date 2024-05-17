package roomescape.domain;

public enum MemberRole {

    ADMIN,
    USER;

    public boolean isAdmin() {
        return ADMIN.equals(this);
    }
}
