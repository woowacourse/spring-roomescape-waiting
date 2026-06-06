package roomescape.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private final Long id;
    private final String name;

    public static User create(String name) {
        return new User(null, name);
    }

    public static User of(Long id, String name) {
        return new User(id, name);
    }
}
