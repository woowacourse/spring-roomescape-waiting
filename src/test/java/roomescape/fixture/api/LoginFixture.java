package roomescape.fixture.api;

import roomescape.auth.dto.LoginRequest;

public class LoginFixture {

    public static final String USER_EMAIL = "aaa@gmail.com";
    public static final String USER_PASSWORD = "1234";
    public static final String USER_NAME = "사용자1";
    public static final Long USER_ID = 1L;

    public static final String ADMIN_EMAIL = "admin@gmail.com";
    public static final String ADMIN_PASSWORD = "1234";
    public static final String ADMIN_NAME = "어드민";

    public static LoginRequest userLoginRequest() {
        return new LoginRequest(USER_EMAIL, USER_PASSWORD);
    }

    public static LoginRequest adminLoginRequest() {
        return new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
