package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.controller.dto.request.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationControllerTest {
    String token;

    @BeforeEach
    void setUp() {
        // login
        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("anna@gmail.com", "password"))
                .when().post("/login")
                .cookie("token");
    }

    @DisplayName("어드민 사용자가 예약을 생성하려는 경우 성공한다.")
    @Test
    void saveIfUserIsAdmin() {
        // when & then
        Map<String, Object> reservations = new HashMap<>();
        reservations.put("date", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reservations.put("timeId", 1);
        reservations.put("themeId", 1);
        reservations.put("memberId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .cookie("token", token)
                .when().post("/admin/reservations")
                .then().statusCode(201);

        // then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }

    @DisplayName("권한이 없는 사용자가 예약을 생성하려는 경우 실패한다.")
    @Test
    void cannotSaveIfUserIsMember() {
        // given
        String memberToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("parang@gmail.com", "password"))
                .when().post("/login")
                .cookie("token");

        // when
        Map<String, Object> reservations = new HashMap<>();
        reservations.put("date", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reservations.put("timeId", 1);
        reservations.put("themeId", 1);
        reservations.put("memberId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .cookie("token", memberToken)
                .when().post("/admin/reservations")
                .then().statusCode(403);
    }
}
