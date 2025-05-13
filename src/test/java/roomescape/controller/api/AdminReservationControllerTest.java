package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminReservationControllerTest {

    String token;

    @BeforeEach
    void createToken() {
        token = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithMember())
            .when().post("/login")
            .then().log().all()
            .extract().response().getCookie("token");
    }

    @Test
    @DisplayName("예약 관리 페이지 내에서 예약 추가")
    void createReservation() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", token)
            .body(getTestParamsWithReservation())
            .when().post("/admin/reservations")
            .then().log().all()
            .statusCode(201);

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(4));
    }

    private Map<String, String> getTestParamsWithMember() {
        return Map.of(
            "email", "admin",
            "password", "1234"
        );
    }

    private Map<String, Object> getTestParamsWithReservation() {
        return Map.of(
            "memberId", 1,
            "date", "2024-04-26",
            "timeId", 1,
            "themeId", 1
        );
    }
}
