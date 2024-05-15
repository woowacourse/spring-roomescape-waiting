package roomescape.acceptance.member;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.acceptance.Fixture.customerToken;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_1;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_2;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_3;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MemberReservationResponse;

public class ReservationGetAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("고객이 자신의 예약 목록을 조회한다.")
    @Test
    void getMyReservation_success() {
        TypeRef<List<MemberReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        List<MemberReservationResponse> response = sendGetRequest()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(reservationListFormat);

        assertThat(response).containsExactly(
                MemberReservationResponse.from(PRE_INSERTED_RESERVATION_1),
                MemberReservationResponse.from(PRE_INSERTED_RESERVATION_2),
                MemberReservationResponse.from(PRE_INSERTED_RESERVATION_3)
        );
    }

    private ValidatableResponse sendGetRequest() {
        return RestAssured.given().log().ifValidationFails()
                .cookie("token", customerToken)
                .when().get("/members/reservations")
                .then().log().all();
    }
}
