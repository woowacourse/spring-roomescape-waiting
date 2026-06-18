package roomescape.domain.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import roomescape.domain.theme.dto.command.ThemeCreateCommand;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.fixture.ThemeFixture;
import roomescape.global.error.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;
    private ThemeService themeService;
    private final Clock fixedClock = Clock.fixed(
        Instant.parse("2026-05-08T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, new ThemeMapper(), fixedClock);
    }

    @Nested
    class 테마_목록_조회 {

        @Test
        void 테마가_없으면_빈_목록을_반환한다() {
            when(themeRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());

            assertThat(themeService.getThemes()).isEmpty();
        }

        @Test
        void 활성_테마_목록을_조회한다() {
            // given
            Theme theme1 = Theme.reconstruct(1L, "테마1", "설명1", "https://example.com/1.png", null);
            Theme theme2 = Theme.reconstruct(2L, "테마2", "설명2", "https://example.com/2.png", null);
            when(themeRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(theme1, theme2));

            // when
            List<ThemeResponseDto> result = themeService.getThemes();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ThemeResponseDto::name).containsExactly("테마1", "테마2");
        }
    }

    @Nested
    class 인기_테마_조회 {

        @Test
        void 오늘을_제외하고_직전_7일_기준으로_인기_테마를_조회한다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);
            LocalDate startDate = today.minusDays(7);
            LocalDate endDate = today.minusDays(1);
            Theme popular = Theme.reconstruct(1L, "인기 테마", "설명", "https://example.com/popular.png", null);
            when(themeRepository.findPopularThemesDateBetween(startDate, endDate, PageRequest.of(0, 10)))
                .thenReturn(List.of(popular));

            // when
            List<ThemeResponseDto> result = themeService.getPopularThemes();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("인기 테마");
        }

        @Test
        void 인기_테마가_없으면_빈_목록을_반환한다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);
            when(themeRepository.findPopularThemesDateBetween(today.minusDays(7), today.minusDays(1),
                PageRequest.of(0, 10)))
                .thenReturn(List.of());

            // when & then
            assertThat(themeService.getPopularThemes()).isEmpty();
        }
    }

    @Nested
    class 테마_생성 {

        @Test
        void 테마를_생성한다() {
            // given
            ThemeCreateCommand command = new ThemeCreateCommand(
                ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(),
                ThemeFixture.VALID.getImageUrl()
            );
            Theme saved = Theme.reconstruct(1L,
                ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(),
                ThemeFixture.VALID.getImageUrl(), null);
            when(themeRepository.existsThemeByNameAndDeletedAtIsNull(ThemeFixture.VALID.getName()))
                .thenReturn(false);
            when(themeRepository.save(any(Theme.class))).thenReturn(saved);

            // when
            ThemeResponseDto result = themeService.saveTheme(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo(ThemeFixture.VALID.getName());
        }

        @Test
        void 같은_이름의_테마가_이미_존재하면_예외가_발생한다() {
            // given
            ThemeCreateCommand command = new ThemeCreateCommand(
                ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(),
                ThemeFixture.VALID.getImageUrl()
            );
            when(themeRepository.existsThemeByNameAndDeletedAtIsNull(ThemeFixture.VALID.getName()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> themeService.saveTheme(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 등록된 테마입니다.");
        }
    }

    @Nested
    class 테마_삭제 {

        @Test
        void 테마를_삭제한다() {
            when(themeRepository.existsThemeByIdAndDeletedAtIsNull(1L)).thenReturn(true);

            themeService.deleteThemeById(1L);
        }

        @Test
        void 존재하지_않는_테마_ID이면_예외가_발생한다() {
            // given
            when(themeRepository.existsThemeByIdAndDeletedAtIsNull(999L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> themeService.deleteThemeById(999L))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마를 찾을 수 없습니다.");
        }
    }
}
