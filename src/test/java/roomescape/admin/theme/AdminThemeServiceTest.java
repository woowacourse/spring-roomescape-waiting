package roomescape.admin.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.admin.theme.dto.AdminThemeRequest;
import roomescape.admin.theme.dto.AdminThemeResponse;
import roomescape.admin.theme.dto.AdminThemesResponse;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class AdminThemeServiceTest {

    @Mock
    private AdminThemeRepository adminThemeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private AdminThemeService adminThemeService;

    @Test
    void createTheme_정상_생성() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 50_000L);
        Theme saved = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg", 50_000L);

        when(adminThemeRepository.existsByName("테마1")).thenReturn(false);
        when(adminThemeRepository.save(any(Theme.class))).thenReturn(saved);
        AdminThemeResponse response = adminThemeService.createTheme(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.name()).isEqualTo("테마1")
        );
    }

    @Test
    void createTheme_중복된_이름이면_예외() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 50_000L);

        when(adminThemeRepository.existsByName("테마1")).thenReturn(true);

        assertThatThrownBy(() -> adminThemeService.createTheme(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_THEME_NAME);
    }

    @Test
    void getAllThemes_정상_조회() {
        Theme theme1 = Theme.of(1L, "테마1", "설명1", "url1", 50_000L);
        Theme theme2 = Theme.of(2L, "테마2", "설명2", "url2", 50_000L);

        when(adminThemeRepository.findAll()).thenReturn(List.of(theme1, theme2));

        AdminThemesResponse response = adminThemeService.getAllThemes();

        assertAll(
                () -> assertThat(response.themes()).hasSize(2),
                () -> assertThat(response.themes().get(0).name()).isEqualTo("테마1")
        );
    }

    @Test
    void deleteTheme_정상_삭제() {
        when(adminThemeRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.existsByThemeId(1L)).thenReturn(false);

        adminThemeService.deleteTheme(1L);

        verify(adminThemeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTheme_존재하지_않는_id면_예외() {
        when(adminThemeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminThemeService.deleteTheme(99L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.THEME_ID_NOT_FOUND);
    }

    @Test
    void deleteTheme_예약이_존재하면_예외() {
        when(adminThemeRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.existsByThemeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> adminThemeService.deleteTheme(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TIME_DELETE_NOT_ALLOWED);
    }

    @Test
    void deleteTheme_DB_제약_위반으로_삭제_실패하면_예외() {
        when(adminThemeRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.existsByThemeId(1L)).thenReturn(false);
        doThrow(DataIntegrityViolationException.class).when(adminThemeRepository).deleteById(1L);

        assertThatThrownBy(() -> adminThemeService.deleteTheme(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TIME_DELETE_NOT_ALLOWED);
    }
}
