package roomescape.acceptance.guest;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.MultipleResponse;

import java.time.LocalDate;

import static roomescape.acceptance.PreInsertedData.*;

class ReservationTimeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("날짜와 테마에 해당하는 예약 시간 목록 조회")
    @Test
    void getAvailableTimes_success() {
        LocalDate date = RESERVATION_CUSTOMER1_THEME2_240501_1100.getDate();
        long themeId = RESERVATION_CUSTOMER1_THEME2_240501_1100.getTheme().getId();

        TypeRef<MultipleResponse<AvailableReservationTimeResponse>> availableTimesFormat = new TypeRef<>() {
        };
        MultipleResponse<AvailableReservationTimeResponse> availableReservationTimeResponses
                = RestAssured.given().log().all()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(availableTimesFormat);

        Assertions.assertThat(availableReservationTimeResponses.items()).containsExactlyInAnyOrder(
                AvailableReservationTimeResponse.from(TIME_10_O0, false),
                AvailableReservationTimeResponse.from(TIME_11_00, true),
                AvailableReservationTimeResponse.from(TIME_12_00, true)
        );
    }
}
