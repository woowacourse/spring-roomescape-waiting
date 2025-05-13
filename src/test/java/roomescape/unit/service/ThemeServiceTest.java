// ThemeServiceTest with BDD style and Fakes
package roomescape.unit.service;

import static org.assertj.core.api.Assertions.*;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.ThemeService;
import roomescape.service.request.CreateThemeRequest;
import roomescape.service.response.ThemeResponse;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeReservationTimeRepository;
import roomescape.unit.fake.FakeThemeRepository;

class ThemeServiceTest {

    private final ThemeRepository themeRepository = new FakeThemeRepository();
    private final ReservationRepository reservationRepository = new FakeReservationRepository();
    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeService themeService = new ThemeService(themeRepository, reservationRepository, FIXED_CLOCK);

    @Test
    void 테마를_생성할_수_있다() {
        // given
        CreateThemeRequest request = new CreateThemeRequest("공포", "무섭다", "thumb.jpg");

        // when
        ThemeResponse response = themeService.createTheme(request);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.name()).isEqualTo("공포");
            softly.assertThat(response.description()).isEqualTo("무섭다");
            softly.assertThat(response.thumbnail()).isEqualTo("thumb.jpg");
        });
    }

    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));
        themeService.createTheme(new CreateThemeRequest("로맨스", "달달하다", "love.jpg"));

        // when
        List<ThemeResponse> result = themeService.findAllThemes();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 예약이_없는_테마는_삭제할_수_있다() {
        // given
        ThemeResponse saved = themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));

        // when & then
        assertThatCode(() -> themeService.deleteThemeById(saved.id()))
                .doesNotThrowAnyException();

        assertThat(themeRepository.findById(saved.id())).isEmpty();
    }

    @Test
    void 예약이_있는_테마는_삭제할_수_없다() {
        // given
        ThemeResponse saved = themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));
        ReservationTime time = reservationTimeRepository.save(LocalTime.of(10, 0));
        Reservation reservation = reservationRepository.save(
                new Member(
                        1L,
                        new MemberName("한스"),
                        new MemberEmail("leehyeonsu4888@gmail.com"),
                        new MemberEncodedPassword("dsa"),
                        MemberRole.MEMBER
                ),
                new ReservationDateTime(
                        new ReservationDate(LocalDate.of(2025, 5, 5)), time, FIXED_CLOCK
                ),
                new Theme(
                        1L,
                        new ThemeName("공포"),
                        new ThemeDescription("공포입니다."),
                        new ThemeThumbnail("썸네일")
                )
        );

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(saved.id()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 존재하지_않는_테마는_삭제할_수_없다() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 최근_일주일_인기_테마를_조회할_수_있다() {
        // given
        themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));
        themeService.createTheme(new CreateThemeRequest("로맨스", "달달하다", "love.jpg"));

        // when
        List<ThemeResponse> result = themeService.getWeeklyPopularThemes();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).name()).isEqualTo("공포");
        });
    }
}
