package roomescape.model.member;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum Role {

    USER(false),
    ADMIN(true),
    ;

    private final boolean isAdmin;

    Role(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isNotAdmin() {
        return !isAdmin;
    }

    public static Role asRole(String rawRole) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(rawRole))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
