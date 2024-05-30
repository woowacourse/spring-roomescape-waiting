package roomescape.acceptance.member;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.MyReservationResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER1_THEME2_240501_1100;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER1_THEME2_240501_1200;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER1_THEME3_240502_1100;
import static roomescape.PreInsertedData.RESERVATION_WAITING_CUSTOMER1_THEME3_240502_1200;
import static roomescape.acceptance.Fixture.customer1Token;

class ReservationQueryAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("고객이 자신의 예약 목록을 조회한다.")
    @Test
    void getMyReservation_success() {
        TypeRef<MultipleResponse<MyReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        MultipleResponse<MyReservationResponse> response = sendGetRequest()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(reservationListFormat);

        assertThat(response.items()).containsExactly(
                MyReservationResponse.of(RESERVATION_CUSTOMER1_THEME2_240501_1100, 0L),
                MyReservationResponse.of(RESERVATION_CUSTOMER1_THEME3_240502_1100, 0L),
                MyReservationResponse.of(RESERVATION_CUSTOMER1_THEME2_240501_1200, 0L),
                MyReservationResponse.of(RESERVATION_WAITING_CUSTOMER1_THEME3_240502_1200, 1L)
        );
    }

    private ValidatableResponse sendGetRequest() {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", customer1Token)
                .when().get("/reservations")
                .then().log().all();
    }
}
