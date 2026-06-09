package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.ReservationValidator;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

class ReservationServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-06T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

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
        LocalDate pastDate = LocalDate.now(CLOCK).minusDays(1);
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
        LocalTime now = LocalTime.now(CLOCK).withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = ReservationTime.of(1L, pastTime);

        when(fixture.themeService.getById(1L)).thenReturn(theme);
        when(fixture.reservationTimeService.getById(1L)).thenReturn(time);

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", LocalDate.now(CLOCK), theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("대기자가 없는 예약은 삭제된다")
    void deleteById() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.parse("2026-08-06"), theme, time);
        when(fixture.reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        fixture.reservationService.cancelById(1L);

        verify(fixture.reservationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 삭제할 수 없다")
    void deleteByIdNotFound() {
        Fixture fixture = new Fixture();
        when(fixture.reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fixture.reservationService.cancelById(1L));
    }

    @Test
    @DisplayName("대기자가 있는 예약을 삭제하면 1순위 대기가 예약으로 승격된다")
    void deleteByIdPromotesEarliestWaiting() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.parse("2026-08-06"), theme, time);
        ReservationWaiting earliest = ReservationWaiting
                .createNew(reservation, "아루", LocalDateTime.now(CLOCK))
                .withId(5L);
        when(fixture.reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(fixture.reservationWaitingRepository.findEarliestByReservationId(1L))
                .thenReturn(Optional.of(earliest));

        fixture.reservationService.cancelById(1L);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(fixture.reservationRepository).update(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("아루");
        verify(fixture.reservationWaitingRepository).deleteById(5L);
    }

    @Test
    @DisplayName("지난 예약은 취소할 수 없다")
    void deleteByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now(CLOCK).withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = ReservationTime.of(1L, pastTime);
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.now(CLOCK), theme, time);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.cancelByIdAndName(1L, "쿠다")
        );
    }

    @Test
    @DisplayName("대기자가 있는 내 예약을 취소하면 1순위 대기가 예약으로 승격된다")
    void deleteByIdAndNamePromotesEarliestWaiting() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.now(CLOCK).plusDays(1), theme, time);
        ReservationWaiting earliest = ReservationWaiting
                .createNew(reservation, "아루", LocalDateTime.now(CLOCK))
                .withId(5L);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));
        when(fixture.reservationWaitingRepository.findEarliestByReservationId(1L))
                .thenReturn(Optional.of(earliest));

        fixture.reservationService.cancelByIdAndName(1L, "쿠다");

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(fixture.reservationRepository).update(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("아루");
        verify(fixture.reservationWaitingRepository).deleteById(5L);
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
        when(fixture.reservationRepository.existsByDateAndThemeIdAndTimeIdExcludingId(
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
        when(fixture.reservationRepository.existsByDateAndThemeIdAndTimeIdExcludingId(date, 1L, 2L, 1L))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(1L, "쿠다", date, 2L)
        );
    }

    @Test
    @DisplayName("대기자가 있는 예약은 변경할 수 없다")
    void updateByIdAndNameWithWaitings() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.now(CLOCK).plusDays(1), theme, time);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));
        when(fixture.reservationWaitingRepository.existsByReservationId(1L)).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(
                        1L,
                        "쿠다",
                        LocalDate.now(CLOCK).plusDays(2),
                        2L
                )
        );
    }

    @Test
    @DisplayName("이미 지난 예약은 변경할 수 없다")
    void updateByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now(CLOCK).withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime savedTime = ReservationTime.of(1L, pastTime);
        Reservation reservation = Reservation.of(1L, "쿠다", LocalDate.now(CLOCK), theme, savedTime);

        when(fixture.reservationRepository.findByIdAndName(1L, "쿠다")).thenReturn(Optional.of(reservation));

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(
                        1L,
                        "쿠다",
                        LocalDate.now(CLOCK).plusDays(1),
                        2L
                )
        );
    }

    private static class Fixture {
        private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
        private final ReservationTimeService reservationTimeService = mock(ReservationTimeService.class);
        private final ThemeService themeService = mock(ThemeService.class);
        private final ReservationValidator reservationValidator = new ReservationValidator(CLOCK);
        private final ReservationWaitingRepository reservationWaitingRepository = mock(ReservationWaitingRepository.class);
        private final ReservationService reservationService =
                new ReservationService(
                        reservationRepository,
                        reservationTimeService,
                        themeService,
                        reservationValidator,
                        reservationWaitingRepository
                );
    }
}
