package roomescape.theme.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.application.service.ThemeCommandService;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.theme.ui.dto.CreateThemeWebRequest;
import roomescape.theme.ui.dto.ThemeResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ThemeFacadeImplTest {

    @Mock
    private ThemeQueryService themeQueryService;

    @Mock
    private ThemeCommandService themeCommandService;

    @InjectMocks
    private ThemeFacadeImpl themeFacade;

    @Test
    @DisplayName("모든 테마를 조회한다")
    void getAll() {
        //then
        List<Theme> themes = List.of(
                createTheme(1L, "테마1", "설명1", "썸네일1"),
                createTheme(2L, "테마2", "설명2", "썸네일1")
        );
        given(themeQueryService.getAll()).willReturn(themes);

        //when
        List<ThemeResponse> result = themeFacade.getAll();

        //then
        assertThat(result).hasSize(2);
        then(themeQueryService).should(times(1)).getAll();
    }

    @Test
    @DisplayName("지난 7일간의 테마 랭킹을 조회한다")
    void getRanking() {
        //then
        List<Theme> themes = List.of(
                createTheme(1L, "인기테마1", "설명1", "썸네일1"),
                createTheme(2L, "인기테마2", "설명2", "썸네일2")
        );
        given(themeQueryService.getRanking(any(ReservationDate.class), any(ReservationDate.class), any(Integer.class)))
                .willReturn(themes);
        //when
        List<ThemeResponse> result = themeFacade.getRanking();

        //then
        assertThat(result).hasSize(2);
        then(themeQueryService).should(times(1))
                .getRanking(any(ReservationDate.class), any(ReservationDate.class), any(Integer.class));
    }

    @Test
    @DisplayName("테마를 생성한다")
    void create() {
        //given
        CreateThemeWebRequest request = new CreateThemeWebRequest("새 테마", "새 테마 설명", "썸네일1");
        Theme theme = createTheme(1L, "새 테마", "새 테마 설명", "썸네일1");
        given(themeCommandService.create(any())).willReturn(theme);

        //when
        ThemeResponse result = themeFacade.create(request);

        //then
        assertThat(result).isNotNull();
        then(themeCommandService).should(times(1)).create(any());
    }

    @Test
    @DisplayName("테마를 삭제한다")
    void delete() {
        //given
        Long themeId = 1L;

        //when
        themeFacade.delete(themeId);

        //then
        then(themeCommandService).should(times(1)).delete(any(ThemeId.class));
    }

    private Theme createTheme(Long id, String name, String description, String thumbnail) {
        return Theme.withId(
                ThemeId.from(id),
                ThemeName.from(name),
                ThemeDescription.from(description),
                ThemeThumbnail.from(thumbnail)
        );
    }
}
