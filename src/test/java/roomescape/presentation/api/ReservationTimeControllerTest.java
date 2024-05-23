package roomescape.presentation.api;

import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.THEME_1;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.application.dto.response.AvailableReservationTimeResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.presentation.BaseControllerTest;

class ReservationTimeControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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
    void getAvailableReservationTimes() {
        LocalDateTime now = LocalDateTime.of(2024, 4, 8, 10, 0);
        LocalDate date = LocalDate.of(2024, 4, 9);
        Theme theme = themeRepository.save(THEME_1);

        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        ReservationDetail detail = new ReservationDetail(date, time1, theme);

        Member member = memberRepository.save(MEMBER_1);

        reservationRepository.save(Reservation.create(now, detail, member));

        ExtractableResponse<Response> extractResponse = RestAssured.given().log().all()
                .param("date", date.toString())
                .param("themeId", theme.getId())
                .when().get("/times/available")
                .then().log().all()
                .extract();

        List<AvailableReservationTimeResponse> responses = extractResponse.jsonPath()
                .getList(".", AvailableReservationTimeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);

            softly.assertThat(responses.get(0).timeId()).isEqualTo(time1.getId());
            softly.assertThat(responses.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(responses.get(0).alreadyBooked()).isTrue();

            softly.assertThat(responses.get(1).timeId()).isEqualTo(time2.getId());
            softly.assertThat(responses.get(1).startAt()).isEqualTo("10:00");
            softly.assertThat(responses.get(1).alreadyBooked()).isFalse();
        });
    }
}
