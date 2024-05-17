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
        LocalDate date = PRE_INSERTED_RESERVATION_1.getDate();
        long themeId = PRE_INSERTED_RESERVATION_1.getTheme().getId();

        TypeRef<MultipleResponse<AvailableReservationTimeResponse>> availableTimesFormat = new TypeRef<>() {
        };
        MultipleResponse<AvailableReservationTimeResponse> availableReservationTimeResponses
                = RestAssured.given().log().all()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(availableTimesFormat);

        Assertions.assertThat(availableReservationTimeResponses.items()).containsExactlyInAnyOrder(
                AvailableReservationTimeResponse.from(PRE_INSERTED_RESERVATION_TIME_1, false),
                AvailableReservationTimeResponse.from(PRE_INSERTED_RESERVATION_TIME_2, true),
                AvailableReservationTimeResponse.from(PRE_INSERTED_RESERVATION_TIME_3, true)
        );
    }
}
