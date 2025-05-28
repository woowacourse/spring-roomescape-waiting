package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixture;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @MockitoBean
    private ThemeRepository themeRepository;

    @Test
    void 테마를_생성할_수_있다() {
        // given
        Theme themeToSave = ThemeFixture.createWithoutId();
        Theme savedTheme = Theme.createWithPrimaryKey(themeToSave, 1L);
        ThemeCreateRequest request = new ThemeCreateRequest(
            themeToSave.getName(),
            themeToSave.getDescription(),
            themeToSave.getThumbnail()
        );
        ThemeResponse expected = ThemeResponse.fromTheme(savedTheme);

        when(themeRepository.save(themeToSave))
            .thenReturn(savedTheme);

        // when
        ThemeResponse actual = themeService.create(request);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        List<Theme> allThemes = IntStream.rangeClosed(0, 10)
            .mapToObj(i -> ThemeFixture.create())
            .toList();

        List<ThemeResponse> expected = allThemes.stream()
            .map(ThemeResponse::fromTheme)
            .toList();

        when(themeRepository.findAll())
            .thenReturn(allThemes);

        // when
        List<ThemeResponse> actual = themeService.findAll();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void id_를_이용하여_테마를_삭제할_수_있다() {
        // given
        Long themeId = 1L;

        // when
        themeService.deleteThemeById(themeId);

        // then
        verify(themeRepository).deleteById(themeId);
    }

    @Test
    void 인기_테마를_조회할_수_있다() {
        // given
        List<Theme> popularThemes = IntStream.rangeClosed(0, 10)
            .mapToObj(i -> ThemeFixture.create())
            .toList();

        List<ThemeResponse> expected = popularThemes.stream()
            .map(ThemeResponse::fromTheme)
            .toList();

        when(themeRepository.findTopReservedThemesInPeriod(
            LocalDate.now().minusDays(7),
            LocalDate.now().minusDays(1),
            10))
            .thenReturn(popularThemes);

        // when
        List<ThemeResponse> actual = themeService.findLimitedThemesByPopularDesc();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void id로_테마를_조회할_수_있다() {
        // given
        Theme theme = ThemeFixture.create();
        when(themeRepository.findById(theme.getId())).thenReturn(java.util.Optional.of(theme));

        // when
        Theme actual = themeService.findByIdOrThrow(theme.getId());

        // then
        assertThat(actual).isEqualTo(theme);
    }

    @Test
    void 존재하지_않는_id로_테마를_조회할_경우_예외가_발생한다() {
        // given
        Long nonExistentId = 999L;
        doThrow(new BadRequestException("테스트용 예외"))
            .when(themeRepository).findById(nonExistentId);

        // when & then
        assertThatThrownBy(() -> themeService.findByIdOrThrow(nonExistentId))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("테스트용 예외");
    }
}
