package roomescape.presentation.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.fixture.Fixture;
import roomescape.presentation.BaseControllerTest;

class ThemeControllerTest extends BaseControllerTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 테마를 조회하고 성공할 경우 200을 반환한다.")
    void getAllThemes() {
        themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com/image.jpg"));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .extract();

        List<ThemeResponse> themeResponses = response.jsonPath()
                .getList(".", ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(themeResponses).hasSize(1);
            softly.assertThat(themeResponses)
                    .containsExactly(new ThemeResponse(1L, "테마 이름", "테마 설명", "https://example.com/image.jpg"));
        });
    }

    @Test
    @DisplayName("특정 기간 중 예약이 많은 순으로 인기 테마를 조회하고 성공할 경우 200을 반환한다.")
    void getPopularThemes() {
        LocalDate includedDate = LocalDate.of(2024, 4, 6);
        LocalDate excludedDate = LocalDate.of(2024, 4, 8);

        Member member = memberRepository.save(Fixture.MEMBER_1);

        Theme theme1 = themeRepository.save(Fixture.THEME_1);
        Theme theme2 = themeRepository.save(Fixture.THEME_2);

        ReservationTime time1 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
        ReservationTime time2 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_2);

        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time1, theme2), member));
        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time2, theme2), member));
        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time2, theme1), member));
        reservationRepository.save(new Reservation(new ReservationDetail(excludedDate, time1, theme1), member));
        reservationRepository.save(new Reservation(new ReservationDetail(excludedDate, time2, theme1), member));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .param("startDate", "2024-04-06")
                .param("endDate", "2024-04-07")
                .param("limit", "2")
                .when().get("/themes/popular")
                .then().log().all()
                .extract();

        List<ThemeResponse> popularThemes = response.jsonPath()
                .getList(".", ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(2);

            softly.assertThat(popularThemes.get(0).id()).isEqualTo(theme2.getId());
            softly.assertThat(popularThemes.get(0).name()).isEqualTo(theme2.getName());

            softly.assertThat(popularThemes.get(1).id()).isEqualTo(theme1.getId());
            softly.assertThat(popularThemes.get(1).name()).isEqualTo(theme1.getName());
        });
    }
}
