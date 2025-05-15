package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {

    @DisplayName("Reservation 목록 내용 갯수를 검사한다")
    @Test
    void reservationTest() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Nested
    @DisplayName("예약 생성")
    class ReservationPostTest {

        String loginToken;

        @BeforeEach
        void setUp() {
            SignUpRequestDto signUpRequestDto = new SignUpRequestDto("가이온", "hello@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(200);

            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

            Map<String, String> cookies = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .getCookies();

            loginToken = cookies.get("token");

            ReservationTimeCreateRequestDto reservationTime = new ReservationTimeCreateRequestDto(LocalTime.of(10, 0));

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(reservationTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201);

            ThemeCreateRequestDto theme = new ThemeCreateRequestDto("a", "b", "c");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(theme)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(201);
        }


        @DisplayName("Reservation 입력 테스트")
        @Test
        void addReservationTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);

            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));

            RestAssured.given().cookie("token", loginToken).log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("Reservation 응답의 LocalTime 형식은 xx:xx 이다")
        @Test
        void reservationResponseTest() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);

            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().cookie("token", loginToken).log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .body("[0].time.startAt", equalTo("10:00"));
        }
    }

    @Nested
    @DisplayName("예약 삭제")
    class ReservationDeleteTest {

        String loginToken;

        @BeforeEach
        void setUp() {
            SignUpRequestDto signUpRequestDto = new SignUpRequestDto("가이온", "hello@woowa.com", "password");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(signUpRequestDto)
                    .when().post("/members")
                    .then().log().all()
                    .statusCode(200);

            LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

            Map<String, String> cookies = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(loginRequestDto)
                    .when().post("/login")
                    .getCookies();

            loginToken = cookies.get("token");
        }

        @DisplayName("예약이 존재하면 삭제할 수 있다")
        @Test
        void deleteReservationTest() {
            ReservationTimeCreateRequestDto reservationTime = new ReservationTimeCreateRequestDto(LocalTime.of(10, 0));

            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(reservationTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201);

            ThemeCreateRequestDto theme = new ThemeCreateRequestDto("a", "b", "c");

            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(theme)
                    .when().post("/themes")
                    .then().log().all()
                    .statusCode(201);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("date", "2030-08-05");
            params.put("timeId", 1);
            params.put("themeId", 1);

            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().cookie("token", loginToken).log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().cookie("token", loginToken).log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @DisplayName("존재하지 않는 예약을 삭제할 수 없다")
        @Test
        void invalidReservationIdDeleteTest() {
            RestAssured.given().cookie("token", loginToken).log().all()
                    .when().delete("/reservations/5")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
