package roomescape.member.domain;

public enum Role {
    ADMIN,
    USER,
    ;

    public boolean isAdminRole() {
        return this.equals(ADMIN);
    }
}
