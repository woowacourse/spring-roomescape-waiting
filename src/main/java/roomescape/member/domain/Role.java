package roomescape.member.domain;

public enum Role {
    ADMIN,
    USER,
    ;

    public boolean isNotAdminRole() {
        return !this.equals(ADMIN);
    }
}
