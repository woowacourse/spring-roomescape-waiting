package roomescape.acceptance.guest;

import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_1;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_TIME_1;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_TIME_2;
import static roomescape.acceptance.PreInsertedData.PRE_INSERTED_RESERVATION_TIME_3;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.AvailableReservationTimeResponse;

class ReservationTimeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("날짜와 테마에 해당하는 예약 시간 목록 조회")
    @Test
    void getAvailableTimes_success() {
        LocalDate date = PRE_INSERTED_RESERVATION_1.getDate();
        long themeId = PRE_INSERTED_RESERVATION_1.getTheme().getId();

        TypeRef<List<AvailableReservationTimeResponse>> availableTimesFormat = new TypeRef<>() {
        };
        List<AvailableReservationTimeResponse> availableReservationTimeResponses = RestAssured.given().log().all()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(availableTimesFormat);

        Assertions.assertThat(availableReservationTimeResponses).containsExactlyInAnyOrder(
                AvailableReservationTimeResponse.from(PRE_INSERTED_RESERVATION_TIME_1, false),
                AvailableReservationTimeResponse.from(PRE_INSERTED_RESERVATION_TIME_2, true),
                AvailableReservationTimeResponse.from(PRE_INSERTED_RESERVATION_TIME_3, true)
        );
    }
}
