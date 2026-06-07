package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.domain.Theme;
import roomescape.exception.custom.RankingPeriodEndDateBeforeStartDateException;
import roomescape.exception.custom.RankingPeriodExceedsLimitException;
import roomescape.exception.custom.RankingPeriodPastDateOnlyException;
import roomescape.exception.custom.ThemeNotExistsException;
import roomescape.repository.ThemeRepository;

public class ThemeServiceTest {

    private ThemeService themeService;
    private ThemeRepository themeRepository;
    private Clock clock;

    @BeforeEach
    void beforeEach() {
        themeRepository = Mockito.mock(ThemeRepository.class);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        themeService = new ThemeService(themeRepository, clock);
    }

    @Test
    void saveTest() {
        ThemeCreateRequest request = new ThemeCreateRequest("피즈의 모험", "모험 이야기", "url");
        Theme themeWithoutId = request.toEntity();
        Theme theme = Theme.withId(1L, themeWithoutId);

        when(themeRepository.save(themeWithoutId)).thenReturn(theme);

        assertThat(themeService.save(themeWithoutId)).isEqualTo(theme);
    }

    @Test
    void findAllTest() {
        Theme themeFizz = new Theme(1L, "피즈의 모험", "모험 이야기", "url");
        Theme themeLuke = new Theme(2L, "루크의 모험", "모험 이야기", "url");
        List<Theme> themes = List.of(
                themeFizz, themeLuke
        );

        when(themeRepository.findAll()).thenReturn(themes);

        assertThat(themeService.findAll()).isEqualTo(themes);
    }

    @Test
    void deleteTest() {
        themeService.delete(1L);

        verify(themeRepository, times(1)).deleteById(1L);
    }

    @Test
    void findRankingTest() {
        LocalDate startDate = LocalDate.of(2026, 4, 20);
        LocalDate endDate = LocalDate.of(2026, 5, 1);
        Theme themeFizz = new Theme(1L, "피즈의 모험", "모험 이야기", "url");
        Theme themeLuke = new Theme(2L, "루크의 모험", "모험 이야기", "url");

        List<Theme> ranking = List.of(
                themeFizz, themeLuke
        );

        when(themeRepository.findRanking(startDate, endDate, 10)).thenReturn(ranking);

        assertThat(themeService.findRanking(startDate, endDate)).isEqualTo(ranking);
    }

    @Test
    void findRankingFutureRankingPeriodExceptionTest() {
        LocalDate startDate = LocalDate.of(2026, 5, 3);
        LocalDate endDate = LocalDate.of(2026, 5, 4);

        assertThatThrownBy(() -> themeService.findRanking(startDate, endDate))
                .isInstanceOf(RankingPeriodPastDateOnlyException.class);
    }

    @Test
    void findRankingInvalidRankingPeriodExceptionTest() {
        LocalDate startDate = LocalDate.of(2026, 4, 4);
        LocalDate endDate = LocalDate.of(2026, 4, 3);

        assertThatThrownBy(() -> themeService.findRanking(startDate, endDate))
                .isInstanceOf(RankingPeriodEndDateBeforeStartDateException.class);
    }

    @Test
    void findRankingLongRankingPeriodExceptionTest() {
        LocalDate startDate = LocalDate.of(2024, 4, 4);
        LocalDate endDate = LocalDate.of(2025, 4, 30);

        assertThatThrownBy(() -> themeService.findRanking(startDate, endDate))
                .isInstanceOf(RankingPeriodExceedsLimitException.class);
    }

    @Test
    void findThemeTest() {
        Theme theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url");

        when(themeRepository.findById(theme.getId())).thenReturn(Optional.of(theme));

        assertThat(themeService.findTheme(1L)).isEqualTo(theme);
    }

    @Test
    void findThemeExceptionTest() {
        Theme theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url");

        when(themeRepository.findById(theme.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.findTheme(1L))
                .isInstanceOf(ThemeNotExistsException.class);
    }

    @Test
    void validateExistThemeTest() {
        Theme theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url");

        when(themeRepository.existsById(theme.getId())).thenReturn(true);

        themeService.validateExistTheme(theme.getId());
    }

    @Test
    void validateExistThemeExceptionTest() {
        when(themeRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> themeService.validateExistTheme(1L))
                .isInstanceOf(ThemeNotExistsException.class);
    }
}
