package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.common.Constant.FIXED_CLOCK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.ClockConfig;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.integration.fixture.ReservationDateFixture;
import roomescape.integration.fixture.ReservationDbFixture;
import roomescape.integration.fixture.ReservationTimeDbFixture;
import roomescape.integration.fixture.ThemeDbFixture;
import roomescape.service.ThemeService;
import roomescape.service.request.CreateThemeRequest;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Test
    void 테마를_생성할_수_있다() {
        // given
        var request = new CreateThemeRequest("공포", "무섭다", "thumb.jpg");

        // when
        var response = themeService.createTheme(request);

        // then
        assertSoftly(softly -> {
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
        var result = themeService.findAllThemes();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 예약이_없는_테마는_삭제할_수_있다() {
        // given
        var saved = themeService.createTheme(new CreateThemeRequest("공포", "무섭다", "thumb.jpg"));

        // when // then
        assertThatCode(() -> themeService.deleteThemeById(saved.id()))
                .doesNotThrowAnyException();

        assertThat(themeService.findAllThemes()).isEmpty();
    }

    @Test
    void 예약이_있는_테마는_삭제할_수_없다() {
        // given
        var theme = themeDbFixture.공포();
        var time = reservationTimeDbFixture.예약시간(LocalTime.of(10, 0));
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var reservationDateTime = new ReservationDateTime(
                new ReservationDate(LocalDate.of(2025, 5, 5)), time, FIXED_CLOCK
        );
        reservationDbFixture.예약_생성(
                reservationDateTime.getReservationDate(),
                reservationDateTime.getReservationTime(),
                theme,
                member
        );

        // when // then
        assertThatThrownBy(() -> themeService.deleteThemeById(theme.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 존재하지_않는_테마는_삭제할_수_없다() {
        // when // then
        assertThatThrownBy(() -> themeService.deleteThemeById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 최근_일주일_인기_테마를_조회할_수_있다() {
        // given
        var 공포 = themeDbFixture.공포();
        var 로맨스 = themeDbFixture.로맨스();
        var 한스 = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var 예약시간_10시 = reservationTimeDbFixture.예약시간_10시();
        var 예약날짜_7일전 = ReservationDateFixture.예약날짜_7일전;
        reservationDbFixture.예약_생성(예약날짜_7일전, 예약시간_10시, 공포, 한스);
        reservationDbFixture.예약_생성(예약날짜_7일전, 예약시간_10시, 로맨스, 한스);

        // when
        var result = themeService.getWeeklyPopularThemes();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).name()).isEqualTo("공포");
        });
    }
}
