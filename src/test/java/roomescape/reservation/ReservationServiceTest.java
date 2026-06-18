package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ConflictException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.Theme;
import roomescape.theme.service.ThemeService;

class ReservationServiceTest {

    @Test
    @DisplayName("예약을 저장한다")
    void save() {
        Fixture fixture = new Fixture();
        LocalDate date = LocalDate.parse("2026-08-06");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Reservation savedReservation = Reservation.of(1L, "쿠다", date, theme, time);

        when(fixture.themeService.getById(1L)).thenReturn(theme);
        when(fixture.reservationTimeService.getById(1L)).thenReturn(time);
        when(fixture.reservationRepository.existsByDateAndThemeIdAndTimeId(date, 1L, 1L)).thenReturn(false);
        when(fixture.reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        Reservation saved = fixture.reservationService.save("쿠다", date, theme.getId(), time.getId());

        assertThat(saved).isEqualTo(savedReservation);
    }

    @Test
    @DisplayName("같은 날짜, 테마, 시간 조합의 중복 예약은 저장할 수 없다")
    void saveDuplicateReservation() {
        Fixture fixture = new Fixture();
        LocalDate date = LocalDate.parse("2026-08-06");
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));

        when(fixture.themeService.getById(1L)).thenReturn(theme);
        when(fixture.reservationTimeService.getById(1L)).thenReturn(time);
        when(fixture.reservationRepository.existsByDateAndThemeIdAndTimeId(date, 1L, 1L)).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.save("아루", date, theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("과거 날짜로는 예약할 수 없다")
    void savePastDate() {
        Fixture fixture = new Fixture();
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));

        when(fixture.themeService.getById(1L)).thenReturn(theme);
        when(fixture.reservationTimeService.getById(1L)).thenReturn(time);

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", pastDate, theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("오늘 날짜라도 이미 지난 시간으로는 예약할 수 없다")
    void savePastTimeToday() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = ReservationTime.of(1L, pastTime);

        when(fixture.themeService.getById(1L)).thenReturn(theme);
        when(fixture.reservationTimeService.getById(1L)).thenReturn(time);

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", LocalDate.now(), theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("예약을 삭제한다")
    void deleteById() {
        Fixture fixture = new Fixture();
        when(fixture.reservationRepository.existsById(1L)).thenReturn(false);

        fixture.reservationService.deleteById(1L);

        verify(fixture.reservationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 삭제할 수 없다")
    void deleteByIdNotFound() {
        Fixture fixture = new Fixture();
        when(fixture.reservationRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> fixture.reservationService.deleteById(1L));
    }

    @Test
    @DisplayName("지난 예약은 취소할 수 없다")
    void deleteByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = ReservationTime.of(1L, pastTime);
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.now(), theme, time);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.deleteByIdAndName(1L, "쿠다")
        );
    }

    @Test
    @DisplayName("조회한 이름의 예약 날짜와 시간을 변경한다")
    void updateByIdAndName() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime firstTime = ReservationTime.of(1L, LocalTime.parse("10:00"));
        ReservationTime secondTime = ReservationTime.of(2L, LocalTime.parse("11:00"));
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.parse("2026-08-06"), theme, firstTime);
        Reservation updatedReservation = Reservation.of(1L, "쿠다", LocalDate.parse("2026-08-07"), theme, secondTime);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));
        when(fixture.reservationTimeService.getById(2L)).thenReturn(secondTime);
        when(fixture.reservationRepository.existsByDateAndThemeIdAndTimeIdAndIdNot(
                LocalDate.parse("2026-08-07"),
                1L,
                2L,
                1L
        )).thenReturn(false);
        when(fixture.reservationRepository.update(any(Reservation.class))).thenReturn(updatedReservation);

        Reservation updated = fixture.reservationService.updateByIdAndName(
                1L,
                "쿠다",
                LocalDate.parse("2026-08-07"),
                2L
        );

        assertThat(updated).isEqualTo(updatedReservation);
    }

    @Test
    @DisplayName("조회한 이름의 다른 예약과 시간이 겹치면 변경할 수 없다")
    void updateByIdAndNameDuplicate() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime firstTime = ReservationTime.of(1L, LocalTime.parse("10:00"));
        ReservationTime secondTime = ReservationTime.of(2L, LocalTime.parse("11:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        Reservation reservation = Reservation.of(1L, "쿠다", date, theme, firstTime);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));
        when(fixture.reservationTimeService.getById(2L)).thenReturn(secondTime);
        when(fixture.reservationRepository.existsByDateAndThemeIdAndTimeIdAndIdNot(date, 1L, 2L, 1L))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(1L, "쿠다", date, 2L)
        );
    }

    @Test
    @DisplayName("이미 지난 예약은 변경할 수 없다")
    void updateByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime savedTime = ReservationTime.of(1L, pastTime);
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.now(), theme, savedTime);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(
                        1L,
                        "쿠다",
                        LocalDate.now().plusDays(1),
                        2L
                )
        );
    }

    private static class Fixture {
        private final JpaReservationRepository reservationRepository = mock(JpaReservationRepository.class);
        private final ReservationTimeService reservationTimeService = mock(ReservationTimeService.class);
        private final ThemeService themeService = mock(ThemeService.class);
        private final ReservationValidator reservationValidator = new ReservationValidator();
        private final ReservationService reservationService =
                new ReservationService(reservationRepository, reservationTimeService, themeService, reservationValidator);
    }
}
