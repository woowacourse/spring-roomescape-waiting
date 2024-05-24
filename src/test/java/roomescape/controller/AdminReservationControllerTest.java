package roomescape.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.AuthConstants;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.schedule.dto.ReservationTimeCreateRequest;
import roomescape.service.theme.dto.ThemeRequest;

class AdminReservationControllerTest extends DataInitializedControllerTest {
    private LocalDate date;
    private long timeId;
    private long themeId;
    private String adminToken;
    private String guestToken;
    private long memberId;

    @BeforeEach
    void init() {
        date = LocalDate.now().plusDays(1);
        timeId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest(LocalTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .when().post("/times")
                .then().extract().response().jsonPath().getLong("id");

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        themeId = RestAssured.given().contentType(ContentType.JSON).body(themeRequest)
                .when().post("/themes")
                .then().extract().response().jsonPath().getLong("id");

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin@email.com", "admin123"))
                .when().post("/login")
                .then().log().all().extract().cookie(AuthConstants.AUTH_COOKIE_NAME);

        guestToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest@email.com", "guest123"))
                .when().post("/login")
                .then().log().all().extract().cookie(AuthConstants.AUTH_COOKIE_NAME);

        memberId = 2;
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .body(new AdminReservationRequest(date, memberId, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("예약 취소 성공 테스트")
    @Test
    void deleteReservationSuccess() {
        //given
        var id = RestAssured.given().contentType(ContentType.JSON)
                .cookie(AuthConstants.AUTH_COOKIE_NAME, guestToken)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().extract().body().jsonPath().get("id");

        //when
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .when().delete("/admin/reservations/" + id)
                .then().log().all()
                .assertThat().statusCode(204);

        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .when().get("/reservations")
                .then().log().all()
                .assertThat().body("size()", is(0));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 사용자, 테마")
    @Test
    @Sql("/insert-past-reservation.sql")
    void findByMemberAndTheme() {
        //when & then
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .queryParam("memberId", 1)
                .queryParam("themeId", 2)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(0));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 시작 날짜")
    @Test
    @Sql("/insert-past-reservation.sql")
    void findByDateFrom() {
        //when & then
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .queryParam("dateFrom", LocalDate.now().minusDays(7).toString())
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(2));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 테마")
    @Test
    @Sql("/insert-past-reservation.sql")
    void findByTheme() {
        //when & then
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .queryParam("themeId", 1)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(1));
    }
}
