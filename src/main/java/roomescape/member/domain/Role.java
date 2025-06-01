package roomescape.member.domain;

public enum Role {

    ADMIN,
    USER;

    public static boolean isAdmin(Role role) {
        return role == ADMIN;
    }
}
