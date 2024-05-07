package roomescape.member.domain;

public enum MemberRole {

    USER("USER"),
    ADMIN("ADMIN"),
    GUEST("GUEST");

    private final String role;

    MemberRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
