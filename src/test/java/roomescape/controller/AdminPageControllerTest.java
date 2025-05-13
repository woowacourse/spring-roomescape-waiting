package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.LoginMember;
import roomescape.dto.member.LoginRequest;
import roomescape.fixture.LoginMemberFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class AdminPageControllerTest {

    private String cookie;

    @BeforeEach
    void loginAsAdmin() {
        LoginMember admin = LoginMemberFixture.getAdmin();

        cookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(admin.getPassword(), admin.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @DisplayName("어드민용 index 페이지를 반환한다")
    @Test
    void indexPageTest() {
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("어드민용 reservation 페이지를 반환한다")
    @Test
    void reservationPageTest() {
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("time 페이지를 반환한다")
    @Test
    void reservationTimePageTest() {
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("theme 페이지를 반환한다")
    @Test
    void themePageTest() {
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("일반 유저는 /admin으로 시작하는 페이지에 접근할 수 없다")
    @Test
    void pageAccessTest() {
        LoginMember user = LoginMemberFixture.getUser();
        String userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .when().get("/admin/theme")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(403);
    }
}
