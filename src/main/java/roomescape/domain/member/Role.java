package roomescape.domain.member;

public enum Role {
    MEMBER, ADMIN;

    public static boolean isNotAdmin(final Role other) {
        return Role.ADMIN != other;
    }
}
