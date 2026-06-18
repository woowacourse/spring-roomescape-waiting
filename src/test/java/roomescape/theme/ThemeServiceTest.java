package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

class ThemeServiceTest {

    @Test
    @DisplayName("테마를 저장한다")
    void save() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);
        Theme savedTheme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");

        when(jpaThemeRepository.existsByName("미술관의 밤")).thenReturn(false);
        when(jpaThemeRepository.save(any(Theme.class))).thenReturn(savedTheme);

        Theme saved = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        assertThat(saved).isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("중복된 이름의 테마는 저장할 수 없다")
    void saveDuplicateName() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);

        when(jpaThemeRepository.existsByName("미술관의 밤")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> themeService.save("미술관의 밤", "새 설명", "https://example.com/new-theme.png")
        );
    }

    @Test
    @DisplayName("ID로 테마를 조회한다")
    void getById() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);

        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");

        when(jpaThemeRepository.findById(1L)).thenReturn(Optional.of(theme));

        Theme found = themeService.getById(1L);

        assertThat(found).isEqualTo(theme);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 테마를 조회할 수 없다")
    void getByIdNotFound() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);

        when(jpaThemeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> themeService.getById(1L));
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void deleteById() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);

        when(jpaReservationRepository.existsByThemeId(1L)).thenReturn(true);
        when(jpaThemeRepository.existsById(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> themeService.deleteById(1L));
    }

    @Test
    @DisplayName("예약이 없는 테마를 삭제한다")
    void deleteByIdWithoutReservation() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);

        when(jpaReservationRepository.existsByThemeId(1L)).thenReturn(false);
//        when(jpaThemeRepository.deleteById(1L)).thenReturn(1);
        when(jpaThemeRepository.existsById(1L)).thenReturn(true);
        themeService.deleteById(1L);

        verify(jpaThemeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 테마는 삭제할 수 없다")
    void deleteByIdNotFound() {
        JpaThemeRepository jpaThemeRepository = mock(JpaThemeRepository.class);
        JpaReservationRepository jpaReservationRepository = mock(JpaReservationRepository.class);
        ThemeService themeService = new ThemeService(jpaThemeRepository, jpaReservationRepository);

        when(jpaReservationRepository.existsByThemeId(1L)).thenReturn(false);
//        when(jpaThemeRepository.deleteById(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> themeService.deleteById(1L));
    }
}
