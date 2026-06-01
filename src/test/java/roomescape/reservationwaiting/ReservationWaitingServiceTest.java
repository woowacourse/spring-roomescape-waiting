package roomescape.reservationwaiting;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;
import roomescape.service.reservationwaiting.ReservationWaitingService;

class ReservationWaitingServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-06T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("동시 요청으로 중복 대기 저장 충돌이 발생하면 중복 대기 예외로 변환한다")
    void saveDuplicatedWaitingByDataIntegrityViolation() {
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationWaitingRepository reservationWaitingRepository = mock(ReservationWaitingRepository.class);
        ReservationWaitingService reservationWaitingService = new ReservationWaitingService(
                reservationRepository,
                reservationWaitingRepository,
                CLOCK
        );
        LocalDate date = LocalDate.parse("2026-08-06");
        Reservation reservation = createReservation(date);

        when(reservationRepository.findReservationIdByDateAndThemeIdAndTimeId(date, 1L, 1L))
                .thenReturn(Optional.of(1L));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationWaitingRepository.existsByReservationIdAndName(1L, "아루")).thenReturn(false);
        when(reservationWaitingRepository.save(any(ReservationWaiting.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(
                ConflictException.class,
                () -> reservationWaitingService.save("아루", date, 1L, 1L)
        );
    }

    private Reservation createReservation(final LocalDate date) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = ReservationTime.of(1L, LocalTime.parse("10:00"));

        return Reservation.of(1L, "쿠다", date, theme, time);
    }
}
