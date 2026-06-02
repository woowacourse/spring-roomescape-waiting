package roomescape.feature.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.feature.theme.domain.ThemeDescription;
import roomescape.feature.theme.domain.ThemeImageUrl;
import roomescape.feature.theme.domain.ThemeName;
import roomescape.feature.theme.dto.command.ThemeCreateCommand;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.theme.domain.Theme;
import roomescape.global.domain.EntityStatus;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.fixture.ThemeFixture;
import roomescape.global.error.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private static final long ID = 1L;
    private static final long NOT_EXISTS_THEME_ID = 999L;
    private static final String NAME = ThemeFixture.VALID.getName();
    private static final String DESCRIPTION = ThemeFixture.VALID.getDescription();
    private static final String IMAGE_URL = ThemeFixture.VALID.getImageUrl();

    @Mock
    private ThemeRepository themeRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, new ThemeMapper());
    }

    @Nested
    class 테마_목록_조회 {

        @Test
        void 테마가_없으면_빈_목록을_반환한다() {
            when(themeRepository.findAllByNotDeleted()).thenReturn(List.of());

            assertThat(themeService.getThemes()).isEmpty();
        }

        @Test
        void 활성_테마_목록을_조회한다() {
            // given
            Theme theme1 = Theme.reconstruct(1L, "테마1", "설명1", "https://example.com/1.png", EntityStatus.ACTIVE);
            Theme theme2 = Theme.reconstruct(2L, "테마2", "설명2", "https://example.com/2.png", EntityStatus.ACTIVE);

            ThemeResponseDto expectedResult1 = new ThemeResponseDto(theme1.getId(), theme1.getName(),
                    theme1.getDescription(), theme1.getImageUrl(), false);
            ThemeResponseDto expectedResult2 = new ThemeResponseDto(theme2.getId(), theme2.getName(),
                    theme2.getDescription(), theme2.getImageUrl(), false);

            when(themeRepository.findAllByNotDeleted()).thenReturn(List.of(theme1, theme2));

            // when
            List<ThemeResponseDto> actualResults = themeService.getThemes();

            // then
            assertThat(actualResults).containsExactly(expectedResult1, expectedResult2);

            verify(themeRepository).findAllByNotDeleted();
            verify(themeRepository, never()).findAll();
        }

        @Test
        void 삭제된_테마를_포함한_전체_테마_목록을_조회한다() {
            // given
            Theme active = Theme.reconstruct(1L, "테마1", "설명1", "https://example.com/1.png", EntityStatus.ACTIVE);
            Theme deleted = Theme.reconstruct(2L, "테마2", "설명2", "https://example.com/2.png", EntityStatus.DELETED);

            ThemeResponseDto activeDto = new ThemeResponseDto(active.getId(), active.getName(),
                    active.getDescription(), active.getImageUrl(), false);
            ThemeResponseDto deletedDto = new ThemeResponseDto(deleted.getId(), deleted.getName(),
                    deleted.getDescription(), deleted.getImageUrl(), true);

            when(themeRepository.findAll()).thenReturn(List.of(active, deleted));

            // when
            List<ThemeResponseDto> actualResults = themeService.getAllThemes();

            // then
            assertThat(actualResults).containsExactly(activeDto, deletedDto);

            verify(themeRepository).findAll();
            verify(themeRepository, never()).findAllByNotDeleted();
        }
    }

    @Nested
    class 인기_테마_조회 {

        @Test
        void 오늘을_제외하고_직전_7일_기준으로_인기_테마를_조회한다() {
            // given
            Theme popular = Theme.reconstruct(
                    ID, NAME, DESCRIPTION, IMAGE_URL,
                    EntityStatus.ACTIVE
            );
            ThemeResponseDto expectedResult = new ThemeResponseDto(
                    ID, NAME, DESCRIPTION, IMAGE_URL, false
            );

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(7);
            LocalDate endDate = today.minusDays(1);
            int limit = 10;

            when(themeRepository.findPopularThemesDateBetween(startDate, endDate, limit))
                    .thenReturn(List.of(popular));

            // when
            List<ThemeResponseDto> actualResults = themeService.getPopularThemes();

            // then
            assertThat(actualResults).containsExactly(expectedResult);
        }

        @Test
        void 인기_테마가_없으면_빈_목록을_반환한다() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(7);
            LocalDate endDate = today.minusDays(1);
            int limit = 10;

            when(themeRepository.findPopularThemesDateBetween(startDate, endDate, limit))
                    .thenReturn(List.of());

            // when
            List<ThemeResponseDto> results = themeService.getPopularThemes();

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    class 테마_생성 {

        @Test
        void 테마를_생성한다() {
            // given
            ThemeCreateCommand command = new ThemeCreateCommand(
                    new ThemeName(NAME),
                    new ThemeDescription(DESCRIPTION),
                    new ThemeImageUrl(IMAGE_URL)
            );
            Theme saved = Theme.reconstruct(
                    ID, NAME, DESCRIPTION, IMAGE_URL,
                    EntityStatus.ACTIVE
            );
            ThemeResponseDto expectedResult = new ThemeResponseDto(
                    ID, NAME, DESCRIPTION, IMAGE_URL, false
            );

            when(themeRepository.existsThemeByNameAndNotDeleted(NAME))
                    .thenReturn(false);
            when(themeRepository.save(any(Theme.class))).thenReturn(saved);

            // when
            ThemeResponseDto actualResult = themeService.saveTheme(command);

            // then
            assertThat(actualResult).isEqualTo(expectedResult);

            verify(themeRepository).save(any(Theme.class));
        }

        @Test
        void 같은_이름의_테마가_이미_존재하면_예외가_발생한다() {
            // given
            ThemeCreateCommand command = new ThemeCreateCommand(
                    new ThemeName(NAME),
                    new ThemeDescription(DESCRIPTION),
                    new ThemeImageUrl(IMAGE_URL)
            );
            when(themeRepository.existsThemeByNameAndNotDeleted(NAME))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> themeService.saveTheme(command))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 등록된 테마입니다.");

            verify(themeRepository, never()).save(any(Theme.class));
        }
    }

    @Nested
    class 테마_삭제 {

        @Test
        void 테마를_삭제한다() {
            // given
            when(themeRepository.existsThemeByIdAndNotDeleted(ID)).thenReturn(true);

            // when
            themeService.deleteThemeById(ID);

            // then
            verify(themeRepository).deleteThemeById(ID);
        }

        @Test
        void 존재하지_않는_테마_ID이면_예외가_발생한다() {
            // given
            when(themeRepository.existsThemeByIdAndNotDeleted(NOT_EXISTS_THEME_ID))
                    .thenReturn(false);

            // when & then
            assertThatThrownBy(() -> themeService.deleteThemeById(NOT_EXISTS_THEME_ID))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("테마를 찾을 수 없습니다.");

            verify(themeRepository, never()).deleteThemeById(any());
        }
    }
}
