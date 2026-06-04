package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationScheduleRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.service.theme.ThemeService;
import roomescape.support.FakeReservationRepository;
import roomescape.support.FakeThemeRepository;

class ThemeServiceTest {

    @Test
    @DisplayName("테마를 저장한다")
    void save() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);
        Theme savedTheme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");

        Theme saved = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(fixture.themeRepository.findById(saved.getId())).contains(saved);
    }

    @Test
    @DisplayName("중복된 이름의 테마는 저장할 수 없다")
    void saveDuplicateName() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);

        when(themeRepository.existsByName("미술관의 밤")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> fixture.themeService.save("미술관의 밤", "새 설명", "https://example.com/new-theme.png")
        );
    }

    @Test
    @DisplayName("ID로 테마를 조회한다")
    void getById() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");

        Theme found = fixture.themeService.getById(theme.getId());

        assertThat(found).isEqualTo(theme);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 테마를 조회할 수 없다")
    void getByIdNotFound() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);

        assertThrows(ResourceNotFoundException.class, () -> fixture.themeService.getById(1L));
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void deleteById() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);

        when(reservationScheduleRepository.existsByThemeId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> themeService.deleteById(1L));
    }

    @Test
    @DisplayName("예약이 없는 테마를 삭제한다")
    void deleteByIdWithoutReservation() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);

        when(reservationScheduleRepository.existsByThemeId(1L)).thenReturn(false);
        when(themeRepository.deleteById(1L)).thenReturn(1);

        assertThat(fixture.themeRepository.findById(theme.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 테마는 삭제할 수 없다")
    void deleteByIdNotFound() {
        ThemeRepository themeRepository = mock(ThemeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = new ThemeService(themeRepository, reservationScheduleRepository);

        when(reservationScheduleRepository.existsByThemeId(1L)).thenReturn(false);
        when(themeRepository.deleteById(1L)).thenReturn(0);

    private static class Fixture {
        private final FakeThemeRepository themeRepository = new FakeThemeRepository();
        private final FakeReservationRepository reservationRepository = new FakeReservationRepository();
        private final ThemeService themeService = new ThemeService(themeRepository, reservationRepository);
    }
}
