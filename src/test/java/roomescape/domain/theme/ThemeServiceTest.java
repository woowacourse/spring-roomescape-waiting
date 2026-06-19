package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.ThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void getTopThemes_예약_많은_테마_순서대로_반환() {
        Theme theme1 = Theme.of(1L, "테마1", "설명1", "url1", 50_000L);
        Theme theme2 = Theme.of(2L, "테마2", "설명2", "url2", 50_000L);
        // theme1 이 2번, theme2 가 1번 예약됨
        when(reservationRepository.findThemeIdsByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(1L, 1L, 2L));
        when(themeRepository.findByIds(anyList())).thenReturn(List.of(theme1, theme2));

        List<ThemeResponse> responses = themeService.getTopThemes();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ThemeResponse::name).containsExactly("테마1", "테마2");
    }

    @Test
    void getAllThemes_정상_조회() {
        Theme theme1 = Theme.of(1L, "테마1", "설명1", "url1", 50_000L);
        Theme theme2 = Theme.of(2L, "테마2", "설명2", "url2", 50_000L);
        when(themeRepository.findAll()).thenReturn(List.of(theme1, theme2));

        List<ThemeResponse> responses = themeService.getAllThemes();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ThemeResponse::id).containsExactly(1L, 2L);
    }
}
