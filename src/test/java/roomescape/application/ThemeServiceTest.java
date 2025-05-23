package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateFixture;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.presentation.dto.request.ThemeCreateRequest;
import roomescape.presentation.dto.response.ThemeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThemeServiceTest extends BaseTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private ReservationDbFixture reservationDbFixture;

    @Test
    void 테마를_모두_조회한다() {
        Theme theme = themeDbFixture.공포();
        List<ThemeResponse> responses = themeService.getThemes();
        ThemeResponse response = responses.getFirst();

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.id()).isEqualTo(theme.getId()),
                () -> assertThat(response.name()).isEqualTo(theme.getName()),
                () -> assertThat(response.description()).isEqualTo(theme.getDescription()),
                () ->assertThat(response.thumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @Test
    void 테마를_생성한다() {
        ThemeCreateRequest request = new ThemeCreateRequest("공포", "공포 테마", "공포.jpg");

        ThemeResponse response = themeService.createTheme(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.description()).isEqualTo(request.description()),
                () -> assertThat(response.thumbnail()).isEqualTo(request.thumbnail())
        );
    }

    @Test
    void 테마를_삭제한다() {
        Theme theme = themeDbFixture.공포();
        themeService.deleteThemeById(theme.getId());

        assertThat(themeService.getThemes()).isEmpty();
    }

    @Test
    void 이미_해당_테마의_예약이_존재할때_삭제하면_예외가_발생한다() {
        Theme theme = themeDbFixture.공포();
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Member member = memberDbFixture.한스_사용자();
        reservationDbFixture.예약_한스_25_4_22_10시_공포(member, reservationTime, theme);

        assertThatThrownBy(() -> themeService.deleteThemeById(theme.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_테마를_삭제하면_예외가_발생한다() {
        assertThatThrownBy(() -> themeService.deleteThemeById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 아이디로_테마를_조회한다() {
        Theme theme = themeDbFixture.공포();
        Theme findTheme = themeService.findThemeById(theme.getId());

        assertThat(theme).isEqualTo(findTheme);
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_예외가_발생한다() {
        Long notExistId = 2L;
        assertThatThrownBy(() -> themeService.findThemeById(notExistId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 지난_일주일_간_인기_테마_10개를_조회한다() {
        List<Theme> themes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            themes.add(themeDbFixture.커스텀_테마("테마" + i));
        }

        for (int i = 0; i < 20; i++) {
            addReservation(i, ReservationDateFixture.예약날짜_오늘, reservationTimeDbFixture.예약시간_10시(), themes.get(i));
            addReservation(19 - i, ReservationDateFixture.예약날짜_7일전, reservationTimeDbFixture.예약시간_10시(), themes.get(i));
        }

        List<ThemeResponse> popularThemes = themeService.getPopularThemes();

        assertThat(popularThemes)
                .hasSize(10)
                .extracting(ThemeResponse::name)
                .containsExactlyInAnyOrder("테마0", "테마1", "테마2", "테마3", "테마4", "테마5", "테마6", "테마7", "테마8", "테마9");
    }

    private void addReservation(int count, ReservationDate date, ReservationTime time, Theme theme) {
        Member member = memberDbFixture.한스_사용자();
        for (int i = 0; i < count; i++) {
            reservationDbFixture.예약_생성(member, date, time, theme);
        }
    }
}
