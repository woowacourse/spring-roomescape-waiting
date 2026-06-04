package roomescape.domain.user;

import lombok.Getter;

@Getter
public class User {

    private final Long id;
    private final String name;

    private User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static User create(String name) {
        return new User(null, name);
    }

    public static User of(Long id, String name) {
        return new User(id, name);
    }
}
