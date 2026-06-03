package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ServiceTest;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ThemeException;

class ThemeServiceTest extends ServiceTest {

    @Autowired
    private ThemeService themeService;

    @Test
    void 테마를_생성할_수_있다() {
        // given
        ThemeRequest request = new ThemeRequest(
                "테마1",
                "설명",
                "https://dsf.sdaf"
        );

        // when
        ThemeResponse response = themeService.create(request);

        // then
        assertThat(response)
                .extracting(
                        ThemeResponse::name,
                        ThemeResponse::description,
                        ThemeResponse::thumbnail
                )
                .containsExactly(
                        request.name(),
                        request.description(),
                        request.thumbnail()
                );
    }

    @Test
    void 이름이_같은_테마_생성시_예외가_발생한다() {
        // given
        ThemeRequest request = new ThemeRequest(
                "테마1",
                "설명",
                "https://dsf.sdaf"
        );
        themeService.create(request);

        // when & then
        assertThatThrownBy(() -> themeService.create(request))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_ALREADY_EXISTS.getMessage());

    }

    @Test
    void 테마를_조회한다() {
        // given
        Theme theme1 = fixtureGenerator.saveTheme("테마1", "설명", "https://dsf.sdaf");
        Theme theme2 = fixtureGenerator.saveTheme("테마2", "설명", "https://dsf.sdaf");

        // when
        List<ThemeResponse> themes = themeService.getThemes();

        // then
        assertAll(
                () -> assertThat(themes).hasSize(2),
                () -> assertThat(themes)
                        .extracting(
                                ThemeResponse::id,
                                ThemeResponse::name,
                                ThemeResponse::description,
                                ThemeResponse::thumbnail
                        )
                        .containsExactlyInAnyOrder(
                                tuple(
                                        theme1.getId(),
                                        theme1.getName(),
                                        theme1.getDescription(),
                                        theme1.getThumbnail()
                                ),
                                tuple(
                                        theme2.getId(),
                                        theme2.getName(),
                                        theme2.getDescription(),
                                        theme2.getThumbnail()
                                )
                        )
        );
    }

    @Test
    void 인기_테마를_조회한다() {
        // given
        LocalDate today = LocalDate.of(2026, 5, 31);

        Theme popularTheme = fixtureGenerator.saveTheme("인기 테마", "설명", "https://dsf.sdaf");
        Theme normalTheme = fixtureGenerator.saveTheme("보통 테마", "설명", "https://dsf.sdaf");
        Theme unpopularTheme = fixtureGenerator.saveTheme("비인기 테마", "설명", "https://dsf.sdaf");

        ReservationTime time10 = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime time11 = fixtureGenerator.saveReservationTime(LocalTime.of(11, 0));
        ReservationTime time12 = fixtureGenerator.saveReservationTime(LocalTime.of(12, 0));

        // 인기 테마: 조회 기간 내 예약 3개
        fixtureGenerator.saveReservation("예약자일", today.minusDays(1), time10, popularTheme);
        fixtureGenerator.saveReservation("예약자이", today.minusDays(2), time11, popularTheme);
        fixtureGenerator.saveReservation("예약자삼", today.minusDays(3), time12, popularTheme);

        // 보통 테마: 조회 기간 내 예약 2개
        fixtureGenerator.saveReservation("예약자사", today.minusDays(1), time10, normalTheme);
        fixtureGenerator.saveReservation("예약자오", today.minusDays(2), time11, normalTheme);

        // 비인기 테마: 조회 기간 내 예약 1개
        fixtureGenerator.saveReservation("예약자육", today.minusDays(1), time10, unpopularTheme);

        // 조회 기간 밖 예약: 순위에 반영되면 안 됨
        fixtureGenerator.saveReservation("예약자칠", today, time10, unpopularTheme);
        fixtureGenerator.saveReservation("예약자팔", today.minusDays(8), time11, unpopularTheme);

        // when
        List<ThemeResponse> rankings = themeService.getThemeRankings(today);

        // then
        assertThat(rankings)
                .extracting(ThemeResponse::name)
                .containsExactly(
                        "인기 테마",
                        "보통 테마",
                        "비인기 테마"
                );
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        ThemeResponse response = themeService.create(new ThemeRequest(
                "테마1",
                "설명",
                "https://dsf.sdaf"
        ));
        int beforeSize = themeService.getThemes().size();

        // when
        themeService.delete(response.id());

        // then
        List<ThemeResponse> themes = themeService.getThemes();
        assertAll(
                () -> assertThat(themes).hasSize(beforeSize - 1),
                () -> assertThat(themes)
                        .extracting(ThemeResponse::id)
                        .doesNotContain(response.id())
        );
    }

    @Test
    void 존재하지_않는_테마_삭제시_예외가_발생한다() {
        // given
        long invalidThemeId = 0L;

        // when & then
        assertThatThrownBy(() -> themeService.delete(invalidThemeId))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void 테마_삭제시_관련_예약이_존재하면_예외가_발생한다() {
        // given
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://dsf.sdaf");
        ReservationTime reservationTime = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));

        fixtureGenerator.saveReservation("예약1", LocalDate.of(2026, 5, 8), reservationTime, theme);

        // when & then
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(ThemeException.class)
                .hasMessage(ThemeErrorCode.THEME_HAS_RESERVATION.getMessage());
    }
}
