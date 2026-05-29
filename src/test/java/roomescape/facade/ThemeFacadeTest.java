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
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.ThemeService;
import roomescape.service.dto.request.ServiceThemeCreateRequest;
import roomescape.service.dto.response.ServiceThemeResponse;

public class ThemeFacadeTest {

    private ThemeFacade themeFacade;
    private ReservationService reservationService;
    private ThemeService themeService;

    @BeforeEach
    void beforeEach() {
        reservationService = Mockito.mock(ReservationService.class);
        themeService = Mockito.mock(ThemeService.class);

        themeFacade = new ThemeFacade(reservationService, themeService);
    }

    @Test
    void saveTest() {
        Theme theme = new Theme(1L, "루크의 모험", "모험 이야기", "url");
        ServiceThemeCreateRequest request = new ServiceThemeCreateRequest("루크의 모험", "모험 이야기", "url");
        ServiceThemeResponse response = ServiceThemeResponse.from(theme);

        when(themeService.save(request)).thenReturn(response);

        assertThat(themeFacade.save(request)).isEqualTo(response);
    }

    @Test
    void findAllTest() {
        Theme themeLuke = new Theme(1L, "루크의 모험", "모험 이야기", "url");
        Theme themeFizz = new Theme(2L, "피즈의 모험", "모험 이야기", "url");

        List<ServiceThemeResponse> responses = List.of(ServiceThemeResponse.from(themeLuke),
                ServiceThemeResponse.from(themeFizz));

        when(themeService.findAll()).thenReturn(responses);

        assertThat(themeFacade.findAll()).isEqualTo(responses);
    }

    @Test
    void findRankingTest() {
        Theme themeLuke = new Theme(1L, "루크의 모험", "모험 이야기", "url");
        Theme themeFizz = new Theme(2L, "피즈의 모험", "모험 이야기", "url");
        LocalDate startDate = LocalDate.of(2026, 4, 20);
        LocalDate endDate = LocalDate.of(2026, 5, 1);

        List<ServiceThemeResponse> responseRanking = List.of(ServiceThemeResponse.from(themeLuke),
                ServiceThemeResponse.from(themeFizz));

        when(themeService.findRanking(startDate, endDate)).thenReturn(responseRanking);

        assertThat(themeFacade.findRanking(startDate, endDate)).isEqualTo(responseRanking);
    }

    @Test
    void deleteTest() {
        Theme theme = new Theme(1L, "루크의 모험", "모험 이야기", "url");

        themeFacade.delete(theme.getId());

        verify(reservationService, times(1)).validateReferencedTheme(theme.getId());
        verify(themeService, times(1)).delete(theme.getId());
    }
}
