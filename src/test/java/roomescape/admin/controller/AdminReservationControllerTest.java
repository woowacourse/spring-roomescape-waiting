package roomescape.admin.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.InitialDataFixture.INITIAL_RESERVATION_COUNT;
import static roomescape.InitialMemberFixture.COMMON_PASSWORD;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_1;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_4;

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
class AdminReservationControllerTest {

    @Test
    @DisplayName("토큰 없이 관리자 예약을 시도하면 예외가 발생한다.")
    void throwExceptionWhenAddAdminReservationWithoutToken() {
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("date", LocalDate.now().plusDays(1).toString());
        reservationParams.put("themeId", "1");
        reservationParams.put("timeId", "1");
        reservationParams.put("memberId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("관리자가 아닌 사용자가 관리자 예약을 시도하면 예외가 발생한다.")
    void throwExceptionWhenNormalUserAddAdminReservation() {
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", COMMON_PASSWORD.password());
        memberParam.put("email", LOGIN_MEMBER_1.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("date", LocalDate.now().plusDays(1).toString());
        reservationParams.put("themeId", "1");
        reservationParams.put("timeId", "1");
        reservationParams.put("memberId", "1");

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("관리자가 예약 요칭 시, 요청 Body에 담긴 정보를 바탕으로 예약을 추가한다.")
    void AdminAddReservationFromRequestBody() {
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", COMMON_PASSWORD.password());
        memberParam.put("email", LOGIN_MEMBER_4.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("date", LocalDate.now().plusDays(1).toString());
        reservationParams.put("themeId", "1");
        reservationParams.put("timeId", "1");
        reservationParams.put("memberId", "1");

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(INITIAL_RESERVATION_COUNT + 1))
                .header("Location", String.format("/reservations/%d", INITIAL_RESERVATION_COUNT + 1));
    }
}
