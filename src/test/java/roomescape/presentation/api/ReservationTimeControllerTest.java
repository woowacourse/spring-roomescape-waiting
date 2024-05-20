package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.application.dto.request.ReservationTimeRequest;
import roomescape.application.dto.response.AvailableReservationTimeResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.presentation.BaseControllerTest;

class ReservationTimeControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("이용한 가능한 시간들을 조회하고, 성공하면 200을 반환한다.")
    void getAllReservationTimes() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .extract();

        List<ReservationTimeResponse> reservationTimeResponses = response.jsonPath()
                .getList(".", ReservationTimeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(reservationTimeResponses).hasSize(1);
            softly.assertThat(reservationTimeResponses)
                    .containsExactly(new ReservationTimeResponse(1L, LocalTime.of(10, 30)));
        });
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회하고 성공하면 200을 반환한다.")
    @Sql("/available-reservation-times.sql")
    void getAvailableReservationTimes() {
        ExtractableResponse<Response> extractResponse = RestAssured.given().log().all()
                .param("date", "2024-04-09")
                .param("themeId", 1L)
                .when().get("/times/available")
                .then().log().all()
                .extract();

        List<AvailableReservationTimeResponse> responses = extractResponse.jsonPath()
                .getList(".", AvailableReservationTimeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(4);

            softly.assertThat(responses.get(0).timeId()).isEqualTo(1L);
            softly.assertThat(responses.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(responses.get(0).alreadyBooked()).isFalse();

            softly.assertThat(responses.get(1).timeId()).isEqualTo(2L);
            softly.assertThat(responses.get(1).startAt()).isEqualTo("12:00");
            softly.assertThat(responses.get(1).alreadyBooked()).isTrue();

            softly.assertThat(responses.get(2).timeId()).isEqualTo(3L);
            softly.assertThat(responses.get(2).startAt()).isEqualTo("17:00");
            softly.assertThat(responses.get(2).alreadyBooked()).isFalse();

            softly.assertThat(responses.get(3).timeId()).isEqualTo(4L);
            softly.assertThat(responses.get(3).startAt()).isEqualTo("21:00");
            softly.assertThat(responses.get(3).alreadyBooked()).isTrue();
        });
    }
}
