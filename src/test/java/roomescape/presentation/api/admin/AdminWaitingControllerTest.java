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
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
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

class AdminWaitingControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("예약 대기 목록을 조회하고 성공하면 200을 반환한다.")
    void getReservationWaitings() {
        Member admin = memberRepository.save(Fixture.MEMBER_ADMIN);
        String token = tokenProvider.createToken(admin.getId().toString());

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationDetail reservationDetail = new ReservationDetail(date, reservationTime, theme);
        waitingRepository.save(new Waiting(reservationDetail, member));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().get("/admin/waitings")
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
        });
    }

    @Test
    @DisplayName("예약 대기에서 예약으로 변경을 승인하고 성공하면 200을 반환한다.")
    void approveReservationWaiting() {
        Member admin = memberRepository.save(Fixture.MEMBER_ADMIN);
        String token = tokenProvider.createToken(admin.getId().toString());

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationDetail reservationDetail = new ReservationDetail(date, reservationTime, theme);
        Waiting savedWaiting = waitingRepository.save(new Waiting(reservationDetail, member));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/admin/waitings/" + savedWaiting.getId() + "/approve")
                .then().log().all()
                .extract();

        ReservationResponse reservationResponse = response.as(ReservationResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
            softly.assertThat(reservationRepository.findById(reservationResponse.id())).isPresent();
        });
    }

    @Test
    @DisplayName("예약 대기에서 예약으로 변경을 거부하고 성공하면 200을 반환한다.")
    void rejectReservationWaiting() {
        Member admin = memberRepository.save(Fixture.MEMBER_ADMIN);
        String token = tokenProvider.createToken(admin.getId().toString());

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationDetail reservationDetail = new ReservationDetail(date, reservationTime, theme);
        Waiting savedWaiting = waitingRepository.save(new Waiting(reservationDetail, member));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/admin/waitings/" + savedWaiting.getId() + "/reject")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(200);
            softly.assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
        });
    }
}
