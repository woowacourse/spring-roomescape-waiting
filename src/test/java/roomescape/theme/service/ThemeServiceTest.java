package roomescape.theme.service;

import org.junit.jupiter.api.BeforeEach;
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
    void 테마를_생성한다() {
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
    void 최근_예약이_많은_테마_조회_기간을_계산한다() {
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
    void 존재하지_않는_테마를_삭제하면_예외가_발생한다() {
        // given
        themeRepository.failToDelete();

        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 해당_테마에_예약이_있으면_테마_삭제시_예외가_발생한다() {
        // given
        themeRepository.failToDeleteByInUse();

        // when & then
        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(ThemeInUseException.class);
    }

}
