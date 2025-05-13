package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
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
import roomescape.dto.time.ReservationTimeCreateRequest;
import roomescape.fixture.LoginMemberFixture;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class ReservationTimeControllerTest {

    private String userCookie;
    private String adminCookie;

    @BeforeEach
    void login() {
        LoginMember admin = LoginMemberFixture.getAdmin();

        adminCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(admin.getPassword(), admin.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        LoginMember user = LoginMemberFixture.getUser();

        userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @Nested
    @DisplayName("예약시간 조회")
    class ReservationTimeGetTest {

        @DisplayName("예약 시간 목록을 조회한다")
        @Test
        void timesTest() {
            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }

    @Nested
    @DisplayName("예약시간 생성")
    class ReservationTimePostTest {

        @DisplayName("어드민은 /times API를 통해 ReservationTime을 생성할 수 있다")
        @Test
        void addReservationTimeTest() {
            ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(15, 30));

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(2));

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("일반 유저는 /times API를 통해 ReservationTime을 생성할 수 없다")
        @Test
        void addReservationTimeExceptionTest1() {
            ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(15, 30));

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(403);
        }

        @DisplayName("중복된 시간은 생성할 수 없다")
        @Test
        void addReservationTimeExceptionTest2() {
            ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(9, 0));

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("유효한 시간만 생성 가능하다")
        @Test
        void invalidRequestTimeTest() {
            Map<String, String> params = new HashMap<>();
            params.put("startAt", "25:40");

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약시간 삭제")
    class ReservationTimeDeleteTest {

        @DisplayName("지정된 예약 시간을 삭제할 수 있다")
        @Test
        void deleteTimeTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @DisplayName("예약된 내역이 존재하는 시간은 삭제할 수 없다")
        @Test
        void deleteTimeExceptionTest1() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(409);
        }

        @DisplayName("일반 유저는 /times API를 통해 ReservationTime을 삭제할 수 없다")
        @Test
        void deleteTimeExceptionTest2() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", userCookie)
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(403);
        }

        @DisplayName("존재하지 않는 Id의 ReservationTime을 삭제할 수 없다")
        @Test
        void invalidTimeIdTest() {
            RestAssured.given().log().all()
                    .header("Cookie", adminCookie)
                    .when().delete("/times/5")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
