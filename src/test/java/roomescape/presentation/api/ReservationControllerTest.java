package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationStatus;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.fixture.Fixture;
import roomescape.presentation.BaseControllerTest;
import roomescape.presentation.dto.request.ReservationWebRequest;

class ReservationControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("나의 예약들을 예약 대기 순번과 함께 조회하고, 성공하면 200을 반환한다.")
    void getMyReservations() {
        // given
        LocalDate date = LocalDate.of(2024, 4, 6);

        Member user1 = memberRepository.save(Fixture.MEMBER_1);
        Member user2 = memberRepository.save(Fixture.MEMBER_2);

        Theme theme1 = themeRepository.save(Fixture.THEME_1);

        ReservationTime time1 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
        ReservationTime time2 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_2);

        Reservation reservation = reservationRepository
                .save(new Reservation(new ReservationDetail(date, time1, theme1), user1));
        reservationRepository.save(new Reservation(new ReservationDetail(date, time1, theme1), user2));
        Waiting waiting = waitingRepository.save(new Waiting(new ReservationDetail(date, time2, theme1), user1));

        String token = tokenProvider.createToken(user1.getId().toString());
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations/mine")
                .then().log().all()
                .extract();

        List<MyReservationResponse> responses = response.jsonPath()
                .getList(".", MyReservationResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(responses).hasSize(2);

            softly.assertThat(responses.get(0).id()).isEqualTo(reservation.getId());
            softly.assertThat(responses.get(0).rank()).isEqualTo(0);
            softly.assertThat(responses.get(0).status()).isEqualTo(ReservationStatus.RESERVED);

            softly.assertThat(responses.get(1).id()).isEqualTo(waiting.getId());
            softly.assertThat(responses.get(1).rank()).isEqualTo(1);
            softly.assertThat(responses.get(1).status()).isEqualTo(ReservationStatus.WAITING);
        });
    }

    @Nested
    @DisplayName("예약을 생성하는 경우")
    class AddReservation {

        @Test
        @DisplayName("성공할 경우 201을 반환한다.")
        void addReservation() {
            Member user = memberRepository.save(Fixture.MEMBER_USER);
            String token = tokenProvider.createToken(user.getId().toString());

            // given
            ReservationTime time = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
            Theme theme = themeRepository.save(Fixture.THEME_1);

            ReservationWebRequest request = new ReservationWebRequest(LocalDate.of(2024, 4, 9), 1L, 1L);

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/reservations")
                    .then().log().all()
                    .extract();

            ReservationResponse reservationResponse = response.as(ReservationResponse.class);
            MemberResponse memberResponse = reservationResponse.member();
            ReservationTimeResponse reservationTimeResponse = reservationResponse.time();
            ThemeResponse themeResponse = reservationResponse.theme();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
                softly.assertThat(response.header("Location")).isEqualTo("/reservations/1");

                softly.assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2024, 4, 9));
                softly.assertThat(memberResponse).isEqualTo(MemberResponse.from(user));
                softly.assertThat(reservationTimeResponse).isEqualTo(ReservationTimeResponse.from(time));
                softly.assertThat(themeResponse).isEqualTo(ThemeResponse.from(theme));
            });
        }

        @Test
        @DisplayName("지나간 날짜/시간이면 400을 반환한다.")
        void failWhenDateTimePassed() {
            Member user = memberRepository.save(Fixture.MEMBER_USER);
            String token = tokenProvider.createToken(user.getId().toString());

            // given
            reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
            themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));

            ReservationWebRequest request = new ReservationWebRequest(LocalDate.of(2024, 4, 7), 1L, 1L);

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/reservations")
                    .then().log().all()
                    .extract();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                softly.assertThat(response.body().asString()).contains("지나간 날짜/시간에 대한 예약은 불가능합니다.");
            });
        }
    }
}
