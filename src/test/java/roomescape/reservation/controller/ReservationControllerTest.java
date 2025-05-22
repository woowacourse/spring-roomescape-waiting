package roomescape.reservation.controller;

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
import roomescape.auth.dto.LoginRequest;
import roomescape.fixture.LoginMemberFixture;
import roomescape.member.domain.Member;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class ReservationControllerTest {

    private String adminCookie;
    private String userCookie;

    @BeforeEach
    void loginAsUser() {
        Member user = LoginMemberFixture.getUser();

        userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Member admin = LoginMemberFixture.getAdmin();

        adminCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(admin.getPassword(), admin.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @Nested
    @DisplayName("에약 조회")
    class ReservationGetTest {

        @DisplayName("예약 목록을 조회한다")
        @Test
        void reservationsTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("예약 생성")
    class ReservationPostTest {

        @DisplayName("Reservation을 생성할 수 있다")
        @Test
        void addReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(2));

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("동일한 날짜, 시간, 테마로는 예약을 할 수 없다")
        @Test
        void addReservationExceptionTest2() {
            Map<String, Object> params = new HashMap<>();
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 삭제")
    class ReservationDeleteTest {

        @DisplayName("존재하는 예약을 삭제할 수 있다")
        @Test
        void deleteReservationTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @DisplayName("존재하지 않는 예약을 삭제할 수 없다")
        @Test
        void invalidReservationIdDeleteTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/reservations/5")
                    .then().log().all()
                    .statusCode(404);
        }

        @DisplayName("일반 유저는 /reservations API를 통해 Reservation을 삭제할 수 없다")
        @Test
        void deleteReservationExceptionTest() {
            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(403);
        }
    }
}
