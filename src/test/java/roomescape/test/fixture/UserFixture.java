package roomescape.test.fixture;

import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.business.UserCreationContent;

public class UserFixture {

    public static UserCreationContent createRequestDto(Role role, String name, String email, String password) {
        return new UserCreationContent(role, name, email, password);
    }

    public static User create(Role role, String name, String email, String password) {
        return User.createWithoutId(role, name, email, password);
    }
}
