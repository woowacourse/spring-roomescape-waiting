package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
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
import roomescape.presentation.BaseControllerTest;
import roomescape.presentation.dto.request.ReservationWebRequest;

@Sql("/member.sql")
class ReservationWaitingControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 대기를 추가하고 성공할 경우 201을 반환한다.")
    void addReservationWaiting() {
        userLogin();

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "ex", Role.USER));

        reservationRepository.save(new Reservation(
                LocalDate.of(2024, 4, 9),
                member,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        ReservationWebRequest request = new ReservationWebRequest(LocalDate.of(2024, 4, 9), 1L, 1L);

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .extract();

        ReservationResponse reservationResponse = response.as(ReservationResponse.class);
        MemberResponse memberResponse = reservationResponse.member();
        ReservationTimeResponse reservationTimeResponse = reservationResponse.time();
        ThemeResponse themeResponse = reservationResponse.theme();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(response.header("Location")).isEqualTo("/reservations/2/waiting");

            softly.assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2024, 4, 9));
            softly.assertThat(reservationTimeResponse).isEqualTo(ReservationTimeResponse.from(reservationTime));
            softly.assertThat(themeResponse).isEqualTo(ThemeResponse.from(theme));
        });
    }

    @Test
    @DisplayName("예약 대기를 제거하고 성공할 경우 200을 반환한다.")
    void deleteReservationWaiting() {
        userLogin();

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.getById(2L);

        reservationRepository.save(new Reservation(
                LocalDate.of(2024, 4, 9),
                member,
                reservationTime,
                theme,
                ReservationStatus.WAITING
        ));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/waiting/1")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
            softly.assertThat(reservationRepository.findById(1L)).isEmpty();
        });
    }
}
