package roomescape.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.AuthConstants;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.schedule.dto.ReservationTimeCreateRequest;
import roomescape.service.theme.dto.ThemeRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql("/truncate-with-guests.sql")
class ReservationWaitingControllerTest {
    @LocalServerPort
    private int port;

    private LocalDate date;
    private long timeId;
    private long themeId;
    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        date = LocalDate.now().plusDays(1);
        timeId = (int) RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest(LocalTime.now()))
                .when().post("/times")
                .then().extract().response().jsonPath().get("id");

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        themeId = (int) RestAssured.given().contentType(ContentType.JSON).body(themeRequest)
                .when().post("/themes")
                .then().extract().response().jsonPath().get("id");

        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie(AuthConstants.AUTH_COOKIE_NAME);
    }

    @DisplayName("예약 대기 추가 성공 테스트")
    @Test
    void createReservationWaiting() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(AuthConstants.AUTH_COOKIE_NAME, token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }
}
