package roomescape.presentation.api.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.presentation.BaseControllerTest;

@Sql("/member.sql")
class AdminWaitingControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 대기 목록을 조회하고 성공하면 200을 반환한다.")
    void getReservationWaitings() {
        adminLogin();

        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        reservationRepository.save(new Reservation(date, member, reservationTime, theme, ReservationStatus.WAITING));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .extract();

        List<ReservationResponse> reservations = response.jsonPath()
                .getList(".", ReservationResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(reservations).hasSize(1);

            softly.assertThat(reservations.get(0).date()).isEqualTo(date);
            softly.assertThat(reservations.get(0).theme()).isEqualTo(ThemeResponse.from(theme));
            softly.assertThat(reservations.get(0).member()).isEqualTo(MemberResponse.from(member));
            softly.assertThat(reservations.get(0).time()).isEqualTo(ReservationTimeResponse.from(reservationTime));
            softly.assertThat(reservations.get(0).status()).isEqualTo(ReservationStatus.WAITING);
        });
    }

    @Test
    @DisplayName("예약 대기에서 예약으로 변경을 승인하고 성공하면 200을 반환한다.")
    void approveReservationWaiting() {
        adminLogin();

        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        reservationRepository.save(new Reservation(date, member, reservationTime, theme, ReservationStatus.WAITING));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/admin/reservations/waiting/1/approve")
                .then().log().all()
                .extract();

        ReservationResponse reservationResponse = response.as(ReservationResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(reservationResponse.status()).isEqualTo(ReservationStatus.RESERVED);
        });
    }

    @Test
    @DisplayName("예약 대기에서 예약으로 변경을 거부하고 성공하면 200을 반환한다.")
    void rejectReservationWaiting() {
        adminLogin();

        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        reservationRepository.save(new Reservation(date, member, reservationTime, theme, ReservationStatus.WAITING));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/waiting/1/reject")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(reservationRepository.findById(1L)).isEmpty();
        });
    }
}
