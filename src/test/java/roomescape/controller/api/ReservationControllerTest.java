package roomescape.controller.api;

import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {

    @Test
    @DisplayName("/reservations 요청 시 예약 정보 조회")
    void readReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("예약 관리 페이지 내에서 예약 삭제")
    void deleteReservation() {
        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200);
    }

    private Map<String, String> getTestParamsWithMember() {
        return Map.of(
            "email", "sa123",
            "password", "na123"
        );
    }
}
