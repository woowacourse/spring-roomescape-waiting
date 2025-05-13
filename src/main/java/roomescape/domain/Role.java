package roomescape.domain;

public enum Role {
    ADMIN, USER;

    public static boolean isAdmin(Role role) {
        return role == ADMIN;
    }

    public static boolean isUser(Role role) {
        return role == USER;
    }
}
