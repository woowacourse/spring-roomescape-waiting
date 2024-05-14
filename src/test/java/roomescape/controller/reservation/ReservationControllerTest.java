package roomescape.controller.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationTimeCreateRequest;
import roomescape.service.reservation.dto.ThemeRequest;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:truncate-with-guests.sql"})
class ReservationControllerTest {
    @LocalServerPort
    private int port;

    private String date;
    private long timeId;
    private long themeId;
    private String token;

    @BeforeEach
    void init() {
        RestAssured.port = port;

        date = "2222-05-01";
        timeId = (int) RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest("17:46"))
                .when().post("/times")
                .then().extract().response().jsonPath().get("id");

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        themeId = (int) RestAssured.given().contentType(ContentType.JSON).body(themeRequest)
                .when().post("/themes")
                .then().extract().response().jsonPath().get("id");

        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("예약 추가 실패 테스트 - 중복 일정 오류")
    @Test
    void createDuplicatedReservation() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations");

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400)
                .body("message", is("선택하신 테마와 일정은 이미 예약이 존재합니다."));
    }

    @DisplayName("예약 추가 실패 테스트 - 일정 오류")
    @Test
    void createInvalidScheduleReservation() {
        //given
        String invalidDate = "2023-10-04";

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(invalidDate, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("현재보다 이전으로 일정을 설정할 수 없습니다."));
    }

    @DisplayName("예약 추가 실패 테스트 - 일정 날짜 오류")
    @Test
    void createInvalidScheduleDateReservation() {
        //given
        String invalidDate = "03-04";

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(invalidDate, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("올바르지 않은 날짜입니다."));
    }

    @DisplayName("모든 예약 내역 조회 테스트")
    @Test
    void findAllReservations() {
        //given
        RestAssured.given().contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations");

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(1));
    }

    @DisplayName("예약 취소 성공 테스트")
    @Test
    void deleteReservationSuccess() {
        //given
        var id = RestAssured.given().contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().extract().body().jsonPath().get("id");

        //when
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/" + id)
                .then().log().all()
                .assertThat().statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .assertThat().body("size()", is(0));
    }

    @DisplayName("예약 취소 실패 테스트 - 본인 예약 아님")
    @Test
    void cannotDeleteReservationSuccess() {
        //given
        var id = RestAssured.given().contentType(ContentType.JSON)
                .cookie("token", token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().extract().body().jsonPath().get("id");

        String wrongToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest2@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        //when
        RestAssured.given().log().all()
                .cookie("token", wrongToken)
                .when().delete("/reservations/" + id)
                .then().log().all()
                .assertThat().statusCode(401);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .assertThat().body("size()", is(1));
    }
}
