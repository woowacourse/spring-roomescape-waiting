package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void getTopThemes_top10_id_순서대로_반환() {
        Theme theme1 = Theme.of(1L, "테마1", "설명1", "url1");
        Theme theme2 = Theme.of(2L, "테마2", "설명2", "url2");
        when(reservationRepository.findThemeIdTop10(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(1L, 2L));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme1));
        when(themeRepository.findById(2L)).thenReturn(Optional.of(theme2));

        List<ThemeResponse> responses = themeService.getTopThemes();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ThemeResponse::name).containsExactly("테마1", "테마2");
    }

    @Test
    void getTopThemes_테마_id가_존재하지_않으면_예외() {
        when(reservationRepository.findThemeIdTop10(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(999L));
        when(themeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.getTopThemes())
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.THEME_ID_NOT_FOUND);
    }

    @Test
    void getAllThemes_정상_조회() {
        Theme theme1 = Theme.of(1L, "테마1", "설명1", "url1");
        Theme theme2 = Theme.of(2L, "테마2", "설명2", "url2");
        when(themeRepository.findAll()).thenReturn(List.of(theme1, theme2));

        List<ThemeResponse> responses = themeService.getAllThemes();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ThemeResponse::id).containsExactly(1L, 2L);
    }
}
