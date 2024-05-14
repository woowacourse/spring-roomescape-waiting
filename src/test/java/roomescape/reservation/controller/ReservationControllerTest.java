package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.InitialDataFixture.INITIAL_RESERVATION_COUNT;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/initial_test_data.sql")
class ReservationControllerTest {

    @Test
    @DisplayName("저장된 reservation을 모두 반환한다.")
    void getReservations() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(INITIAL_RESERVATION_COUNT));
    }

    @Test
    @DisplayName("로그인 후 Reservation을 추가한다.")
    void addReservation() {
        //given
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", "password");
        memberParam.put("email", "admin@email.com");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("date", tomorrow.toString());
        reservationParams.put("timeId", "1");
        reservationParams.put("themeId", "1");

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(INITIAL_RESERVATION_COUNT + 1))
                .header("Location", String.format("/reservations/%d", INITIAL_RESERVATION_COUNT + 1));
    }

    @Test
    @DisplayName("Reservation을 삭제한다.")
    void deleteReservation() {
        //when
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        //then
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(INITIAL_RESERVATION_COUNT - 1));
    }
}
