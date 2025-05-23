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

    //TODO: 테스트 공부하기
/*
    @Test
    @DisplayName("예약 생성 테스트")
    void createReservations(){
        authToken = authService.createToken(new LoginRequest("admin", "1234"));

        ReservationRequest request = new ReservationRequest(
                LocalDate.now().plusDays(1),
                1,
                1
        );

        RestAssured.given().log().all()
                .cookie()
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(204);
    }*/

    @Test
    @DisplayName("/reservations 요청 시 예약 정보 조회")
    void readReservations() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200);
    }

    //TODO: 테스트 공부하기
/*
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
*/

    private Map<String, String> getTestParamsWithMember() {
        return Map.of(
            "email", "admin",
            "password", "1234"

        );
    }
}
