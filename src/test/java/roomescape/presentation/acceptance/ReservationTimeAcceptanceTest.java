package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.dto.ReservationTimeRequest;
import roomescape.application.dto.ReservationTimeResponse;
import roomescape.application.dto.TokenRequest;

class ReservationTimeAcceptanceTest extends AcceptanceTest {
    private String adminToken;

    @BeforeEach
    void adminTokenSetUp() {
        TokenRequest tokenRequest = new TokenRequest("admin@wooteco.com", "wootecoCrew6!");
        adminToken = RestAssured.given()
                .contentType("application/json")
                .body(tokenRequest)
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("token");
    }

    @DisplayName("예약 시간을 추가한다.")
    @Test
    void createReservationTimeTest() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 30));
        ReservationTimeResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ReservationTimeResponse.class);

        assertThat(response.startAt()).isEqualTo("10:30");
    }
}
