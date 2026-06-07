package roomescape.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private final Long id;
    private final String name;
    private final String password;
    private final UserRole role;

    public static User create(String name) {
        return new User(null, name, "", UserRole.USER);
    }

    public static User of(Long id, String name) {
        return new User(id, name, "", UserRole.USER);
    }

    public static User create(String name, String password, UserRole role) {
        return new User(null, name, password, role);
    }

    public static User of(Long id, String name, String password, UserRole role) {
        return new User(id, name, password, role);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
