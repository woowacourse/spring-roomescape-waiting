package roomescape.member.domain;

public enum Role {
    ADMIN("admin"),
    USER("user");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public static Role from(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    public String getRole() {
        return role;
    }
}
