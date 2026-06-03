package roomescape.theme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeInUseException;
import roomescape.theme.service.dto.request.ThemeCreateRequest;
import roomescape.theme.service.dto.response.ThemeResponse;
import roomescape.theme.service.support.FakeThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atStartOfDay(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    private FakeThemeRepository themeRepository;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        themeService = new ThemeService(themeRepository, FIXED_CLOCK);
    }

    @Test
    @DisplayName("테마를 생성한다")
    void createTheme() {
        // when
        ThemeResponse response = themeService.create(
                new ThemeCreateRequest("링", "공포 테마", "http:~")
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("링");
        assertThat(themeRepository.savedTheme().getName()).isEqualTo("링");
    }

    @Test
    @DisplayName("최근 예약이 많은 테마 조회 기간을 계산한다")
    void calculatePopularThemeSearchPeriod() {
        // given
        themeRepository.setPopularThemes(List.of(
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        List<ThemeResponse> responses = themeService.getPopularThemes();

        // then
        assertThat(themeRepository.popularStartDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(themeRepository.popularToday()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(responses)
                .extracting(ThemeResponse::name)
                .containsExactly("링");
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingNonExistingTheme() {
        // given
        themeRepository.failToDelete();

        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("해당 테마에 예약이 있으면 테마 삭제시 예외가 발생한다")
    void throwExceptionWhenDeletingThemeInUse() {
        // given
        themeRepository.failToDeleteByInUse();

        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(ThemeInUseException.class);
    }

}
