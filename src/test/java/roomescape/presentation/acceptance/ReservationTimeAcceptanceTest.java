package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;

class ReservationTimeAcceptanceTest extends AcceptanceTest {
    @DisplayName("예약 시간을 추가한다.")
    @Test
    void createReservationTimeTest() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 30));
        ReservationTimeResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ReservationTimeResponse.class);

        assertThat(response.startAt()).isEqualTo("10:30");
    }
}
