package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.model.member.Role;
import roomescape.util.TokenManager;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminPageControllerTest {

    private static final String LOGIN_USER_TOKEN = TokenManager.create(
            new MemberWithoutPassword(1L, "에버", "treeboss@gmail.com", Role.USER));
    private static final String LOGIN_ADMIN_TOKEN = TokenManager.create(
            new MemberWithoutPassword(2L, "관리자", "admin@gmail.com", Role.ADMIN));

    @DisplayName("관리자가 어드민 페이지에 접속할 경우 예외를 반환하지 않는다.")
    @Test
    void should_throw_exception_when_admin_contact() {
        RestAssured
                .given().log().all()
                .cookie("token", LOGIN_ADMIN_TOKEN)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("일반 유저가 어드민 페이지에 접속할 경우 예외를 반환한다.")
    @Test
    void should_not_throw_exception_when_user_contact() {
        RestAssured
                .given().log().all()
                .cookie("token", LOGIN_USER_TOKEN)
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }
}
