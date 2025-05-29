package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixture;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@SpringBootTest
class ThemeServiceFacadeTest {

    @Autowired
    private ThemeServiceFacade themeServiceFacade;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void 테마를_생성할_수_있다() {
        // given
        ThemeCreateRequest request = new ThemeCreateRequest(
            "테마 이름",
            "테마 설명",
            "썸네일 URL"
        );
        ThemeResponse expected = new ThemeResponse(
            1L,
            "테마 이름",
            "테마 설명",
            "썸네일 URL"
        );

        when(themeService.create(request))
            .thenReturn(expected);

        // when
        ThemeResponse actual = themeServiceFacade.createTheme(request);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 모든_테마를_조회할_수_있다() {
        // given
        List<Theme> allThemes = IntStream.rangeClosed(0, 19)
            .mapToObj(i -> ThemeFixture.create())
            .toList();

        List<ThemeResponse> expected = allThemes.stream()
            .map(ThemeResponse::fromTheme)
            .toList();

        when(themeService.findAll())
            .thenReturn(expected);

        // when
        List<ThemeResponse> actual = themeServiceFacade.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void id를_통해_테마를_삭제할_수_있다() {
        // given
        Long themeId = 1L;

        // when
        themeServiceFacade.deleteThemeById(themeId);

        // then
        verify(themeService).deleteThemeById(themeId);
    }

    @Test
    void 인기_테마를_조회할_수_있다() {
        // given
        List<ThemeResponse> expected = IntStream.rangeClosed(0, 9)
            .mapToObj(i -> ThemeResponse.fromTheme(ThemeFixture.create()))
            .toList();

        when(themeService.findLimitedThemesByPopularDesc())
            .thenReturn(expected);

        // when
        List<ThemeResponse> actual = themeServiceFacade.findPopular();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
