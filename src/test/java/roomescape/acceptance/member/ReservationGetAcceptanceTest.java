package roomescape.acceptance.member;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MemberReservationResponse;
import roomescape.dto.response.MultipleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.acceptance.Fixture.customerToken;
import static roomescape.acceptance.PreInsertedData.*;

class ReservationGetAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("고객이 자신의 예약 목록을 조회한다.")
    @Test
    void getMyReservation_success() {
        TypeRef<MultipleResponse<MemberReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        MultipleResponse<MemberReservationResponse> response = sendGetRequest()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(reservationListFormat);

        assertThat(response.items()).containsExactly(
                MemberReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1100),
                MemberReservationResponse.from(RESERVATION_CUSTOMER1_THEME3_240502_1100),
                MemberReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1200)
        );
    }

    private ValidatableResponse sendGetRequest() {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", customerToken)
                .when().get("/reservations")
                .then().log().all();
    }
}
