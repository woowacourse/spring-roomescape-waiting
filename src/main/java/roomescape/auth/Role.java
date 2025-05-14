package roomescape.auth;

import roomescape.exception.InvalidRoleException;

public enum Role {

    ADMIN("ADMIN"), MEMBER("MEMBER");

    Role(String role) {
        this.role = role;
    }

    private String role;

    public static Role of(String value) {
        try {
            return Role.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException();
        }
    }
}
