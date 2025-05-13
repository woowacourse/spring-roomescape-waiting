package roomescape.user.domain;

public enum UserRole {
    NORMAL,
    ADMIN;

    public boolean includes(final UserRole required) {
        if (this == ADMIN) {
            return true;
        }
        return this == required;
    }
}
