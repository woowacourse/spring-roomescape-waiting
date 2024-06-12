package roomescape.fixture;

import roomescape.controller.dto.LoginRequest;

public class LoginRequestFixture {

    public static LoginRequest createUserRequest() {
        return new LoginRequest("user@a.com", "123a!");
    }

    public static LoginRequest createAdminRequest() {
        return new LoginRequest("admin@a.com", "123a!");
    }
}
