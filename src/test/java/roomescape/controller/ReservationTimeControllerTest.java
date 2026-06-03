package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.AcceptanceTest;
import roomescape.dto.response.AvailableReservationTimeResponse;

public class ReservationTimeControllerTest extends AcceptanceTest {

    @Test
    void 시간을_조회한다() {
        long reservationTimeId = apiFixtureGenerator.createTime("22:00");
        long themeId = apiFixtureGenerator.createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        apiFixtureGenerator.createReservation("러키", reservationDate, reservationTimeId, themeId);

        List<AvailableReservationTimeResponse> responses = RestAssured.given().log().all()
                .when().get("/times?themeId=" + themeId + "&baseDate=" + reservationDate)
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", AvailableReservationTimeResponse.class);

        assertThat(responses)
                .extracting("startAt", "reserved")
                .contains(
                        tuple(LocalTime.of(22, 0), true)
                );
    }

}
