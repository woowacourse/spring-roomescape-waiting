package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.LoginMember;
import roomescape.dto.member.LoginRequest;
import roomescape.fixture.LoginMemberFixture;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class AdminReservationControllerTest {

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

    @Nested
    @DisplayName("에약 조회")
    class ReservationGetTest {

        @DisplayName("주어진 검색 조건에 해당하는 Reservation을 조회할 수 있다")
        @Test
        void searchReservationsTest() {
            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .param("member", 1)
                    .param("theme", 1)
                    .param("from", "2025-05-05")
                    .param("to", "2025-05-30")
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("일반 유저는 예약 검색 API에 접근할 수 없다")
        @Test
        void searchReservationsExceptionTest() {
            LoginMember user = LoginMemberFixture.getUser();
            String userCookie = RestAssured
                    .given().log().all()
                    .body(new LoginRequest(user.getPassword(), user.getEmail()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when().post("/login")
                    .then().log().all().extract().header("Set-Cookie").split(";")[0];

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .param("member", 1)
                    .param("theme", 1)
                    .param("from", "2025-05-05")
                    .param("to", "2025-05-30")
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("예약 생성")
    class ReservationPostTest {

        @DisplayName("어드민은 /admin/reservations API를 통해 Reservation을 생성할 수 있다")
        @Test
        void addReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);
            params.put("memberId", 1);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(2));

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("일반 유저는 /admin/reservations API를 통해 Reservation을 생성할 수 없다")
        @Test
        void addReservationExceptionTest1() {
            LoginMember user = LoginMemberFixture.getUser();
            String userCookie = RestAssured
                    .given().log().all()
                    .body(new LoginRequest(user.getPassword(), user.getEmail()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when().post("/login")
                    .then().log().all().extract().header("Set-Cookie").split(";")[0];

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);
            params.put("memberId", 1);

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(403);
        }
    }
}
