package roomescape.test.fixture;

import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.request.UserCreationRequest;

public class UserFixture {

    public static UserCreationRequest createRequestDto(Role role, String name, String email, String password) {
        return new UserCreationRequest(role, name, email, password);
    }

    public static User create(Role role, String name, String email, String password) {
        return User.createWithoutId(role, name, email, password);
    }
}
