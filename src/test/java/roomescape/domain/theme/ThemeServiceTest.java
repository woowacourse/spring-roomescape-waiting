package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.ThemeCreationRequest;
import roomescape.domain.theme.dto.ThemeCreationResponse;
import roomescape.support.exception.RoomescapeException;

class ThemeServiceTest {

    private ThemeService themeService;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        themeRepository = mock(ThemeRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        ThemeCreationRequest request = new ThemeCreationRequest("테마", "설명", "url");
        when(themeRepository.save(any(Theme.class)))
            .thenReturn(Theme.of(1L, "테마", "설명", "url"));
        
        ThemeCreationResponse response = themeService.createTheme(request);

        assertThat(response.name()).isEqualTo("테마");
    }

    @Test
    @DisplayName("사용 중인 테마를 삭제하려 하면 예외가 발생한다.")
    void deleteInUseTheme() {
        Theme theme = Theme.of(1L, "테마", "설명", "url");
        when(themeRepository.findById(theme.getId())).thenReturn(Optional.of(theme));
        when(reservationRepository.countByThemeId(theme.getId())).thenReturn(1);

        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId()))
            .isInstanceOf(RoomescapeException.class);

        verify(themeRepository, never()).delete(theme);
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하려 하면 예외가 발생한다.")
    void deleteNotFoundTheme() {
        when(themeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
            .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("인기 테마 순위를 조회한다.")
    void getThemeRank() {
        when(themeRepository.findPopularThemes(anyInt(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(Theme.of(1L, "테마1", "설명", "url")));
        
        var responses = themeService.getThemeRank();

        assertThat(responses).isNotNull();
    }
}
