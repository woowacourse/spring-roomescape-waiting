package roomescape.controller.api;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.reservation.controller.dto.request.ReservationRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {

    @Autowired
    private AuthService authService;

    private String authToken;


    @Test
    @DisplayName("/reservations 요청 시 예약 정보 조회")
    void readReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200);
    }


    private Map<String, String> getTestParamsWithMember() {
        return Map.of(
            "email", "admin",
            "password", "1234"

        );
    }
}
