package roomescape.presentation.api.admin;

import static org.assertj.core.api.Assertions.assertThat;

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
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.presentation.BaseControllerTest;
import roomescape.presentation.dto.request.AdminReservationWebRequest;

@Sql("/member.sql")
class AdminReservationControllerTest extends BaseControllerTest {

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
    @DisplayName("조건에 맞는 예약들(예약 대기 제외)을 조회하고 성공할 경우 200을 반환한다.")
    void getReservationsByConditions() {
        adminLogin();

        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        Member member1 = memberRepository.save(new Member("ex2@gmail.com", "password", "유저2", Role.USER));
        reservationRepository.save(new Reservation(new ReservationDetail(date, reservationTime, theme), member));
        waitingRepository.save(new Waiting(new ReservationDetail(date, reservationTime, theme), member1));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/reservations")
                .then().log().all()
                .extract();

        List<ReservationResponse> reservationResponses = response.jsonPath()
                .getList(".", ReservationResponse.class);

        ReservationResponse reservationResponse = reservationResponses.get(0);

        MemberResponse memberResponse = reservationResponse.member();
        ReservationTimeResponse reservationTimeResponse = reservationResponse.time();
        ThemeResponse themeResponse = reservationResponse.theme();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(reservationResponses).hasSize(1);

            softly.assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2024, 4, 9));
            softly.assertThat(memberResponse).isEqualTo(MemberResponse.from(member));
            softly.assertThat(reservationTimeResponse).isEqualTo(ReservationTimeResponse.from(reservationTime));
            softly.assertThat(themeResponse).isEqualTo(ThemeResponse.from(theme));
        });
    }

    @Nested
    @DisplayName("예약을 생성하는 경우")
    class AddReservation {

        @Test
        @DisplayName("성공할 경우 201을 반환한다.")
        void addAdminReservation() {
            adminLogin();

            ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));

            AdminReservationWebRequest request = new AdminReservationWebRequest(
                    LocalDate.of(2024, 6, 22),
                    reservationTime.getId(),
                    theme.getId(),
                    ADMIN_ID
            );

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .extract();

            ReservationResponse reservationResponse = response.as(ReservationResponse.class);
            MemberResponse memberResponse = reservationResponse.member();
            ReservationTimeResponse timeResponse = reservationResponse.time();
            ThemeResponse themeResponse = reservationResponse.theme();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
                softly.assertThat(reservationResponse.date()).isEqualTo(LocalDate.of(2024, 6, 22));
                softly.assertThat(memberResponse)
                        .isEqualTo(new MemberResponse(1L, "admin@gmail.com", "어드민", Role.ADMIN));
                softly.assertThat(timeResponse).isEqualTo(ReservationTimeResponse.from(reservationTime));
                softly.assertThat(themeResponse).isEqualTo(ThemeResponse.from(theme));
            });
        }

        @Test
        @DisplayName("어드민 권한이 아닐 경우 403을 반환한다.")
        void addAdminReservationFailWhenNotAdmin() {
            userLogin();

            AdminReservationWebRequest request = new AdminReservationWebRequest(
                    LocalDate.of(2024, 6, 22),
                    1L,
                    1L,
                    1L
            );

            ExtractableResponse<Response> response = RestAssured.given().log().all()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .extract();

            assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        }
    }

    @Test
    @DisplayName("예약을 삭제하고 성공할 경우 204를 반환한다.")
    void deleteReservationById() {
        adminLogin();

        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "유저", Role.USER));
        ReservationDetail detail = new ReservationDetail(date, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(new Reservation(detail, member));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/admin/reservations/" + savedReservation.getId())
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
