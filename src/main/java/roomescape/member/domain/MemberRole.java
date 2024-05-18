package roomescape.member.domain;

public enum MemberRole {
    USER("USER"),
    ADMIN("ADMIN"),
    ;

    private final String role;

    MemberRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public String getRole() {
        return role;
    }
}
