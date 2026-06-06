package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.response.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;

public class ThemeFacadeTest {

    private ThemeFacade themeFacade;
    private ReservationService reservationService;
    private ThemeService themeService;
    private WaitService waitService;

    @BeforeEach
    void beforeEach() {
        reservationService = Mockito.mock(ReservationService.class);
        themeService = Mockito.mock(ThemeService.class);
        waitService = Mockito.mock(WaitService.class);

        themeFacade = new ThemeFacade(reservationService, themeService, waitService);
    }

    @Test
    void saveTest() {
        Theme themeWithoutId = new Theme("루크의 모험", "모험 이야기", "url");
        Theme theme = Theme.withId(1L, themeWithoutId);
        ThemeCreateRequest request = new ThemeCreateRequest("루크의 모험", "모험 이야기", "url");
        ThemeResponse response = ThemeResponse.from(theme);

        when(themeService.save(themeWithoutId)).thenReturn(theme);

        assertThat(themeFacade.save(request)).isEqualTo(response);
    }

    @Test
    void findAllTest() {
        Theme themeLuke = new Theme(1L, "루크의 모험", "모험 이야기", "url");
        Theme themeFizz = new Theme(2L, "피즈의 모험", "모험 이야기", "url");
        List<Theme> themes = List.of(themeLuke, themeFizz);
        List<ThemeResponse> responses = List.of(ThemeResponse.from(themeLuke),
                ThemeResponse.from(themeFizz));

        when(themeService.findAll()).thenReturn(themes);

        assertThat(themeFacade.findAll()).isEqualTo(responses);
    }

    @Test
    void findRankingTest() {
        Theme themeLuke = new Theme(1L, "루크의 모험", "모험 이야기", "url");
        Theme themeFizz = new Theme(2L, "피즈의 모험", "모험 이야기", "url");
        LocalDate startDate = LocalDate.of(2026, 4, 20);
        LocalDate endDate = LocalDate.of(2026, 5, 1);

        List<Theme> themeRanking = List.of(themeLuke, themeFizz);
        List<ThemeResponse> responseRanking = List.of(ThemeResponse.from(themeLuke),
                ThemeResponse.from(themeFizz));

        when(themeService.findRanking(startDate, endDate)).thenReturn(themeRanking);

        assertThat(themeFacade.findRanking(startDate, endDate)).isEqualTo(responseRanking);
    }

    @Test
    void deleteTest() {
        Theme theme = new Theme(1L, "루크의 모험", "모험 이야기", "url");

        themeFacade.delete(theme.getId());

        verify(reservationService, times(1)).validateReferencedTheme(theme.getId());
        verify(waitService, times(1)).validateReferencedTheme(theme.getId());
        verify(themeService, times(1)).delete(theme.getId());
    }
}
