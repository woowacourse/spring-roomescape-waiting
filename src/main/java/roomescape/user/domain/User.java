package roomescape.user.domain;

import lombok.Getter;
import roomescape.common.exception.BadRequestException;
import roomescape.common.exception.errors.UserErrors;

@Getter
public class User {

    private static final int MAX_NAME_LENGTH = 10;

    private final Long id;
    private final String name;

    private User(Long id, String name) {
        validate(name);
        this.id = id;
        this.name = name;
    }

    public static User createWithoutId(String name) {
        return new User(null, name);
    }

    public static User createWithId(long id, User user) {
        return of(id, user.getName());
    }

    public static User of(long id, String name) {
        return new User(id, name);
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException(UserErrors.INVALID_USER_NAME);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException(UserErrors.INVALID_USER_NAME_LENGTH);
        }
    }
}
