package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.exception.custom.CannotDeleteReservationTimeInUseException;
import roomescape.exception.custom.CannotDeleteThemeInUseException;
import roomescape.exception.custom.ReservationNotExistsException;
import roomescape.repository.ReservationRepository;

public class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    private Clock fixedClock;

    private ReservationTime reservationTime;
    private Theme theme;
    private Slot slot;
    private Slot otherSlot;
    private Member fizz;
    private Member luke;

    @BeforeEach
    void beforeEach() {
        fixedClock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        reservationRepository = Mockito.mock(ReservationRepository.class);
        reservationService = new ReservationService(reservationRepository, fixedClock);

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");

        slot = new Slot(LocalDate.of(2026, 5, 2), reservationTime, theme);
        otherSlot = new Slot(LocalDate.of(2026, 5, 3), reservationTime, theme);
        fizz = new Member(1L, "fizz");
        luke = new Member(2L, "luke");
    }

    @Test
    void saveTest() {
        Reservation reservationWithoutId = new Reservation(fizz, slot);
        Reservation reservation = reservationWithoutId.withId(1L);

        when(reservationRepository.save(reservationWithoutId)).thenReturn(reservation);

        assertThat(reservationService.save(reservationWithoutId, false)).isEqualTo(reservation);
    }

    @Test
    void findByNameTest() {
        List<Reservation> reservations = List.of(
                new Reservation(1L, fizz, slot),
                new Reservation(2L, fizz, otherSlot)
        );

        when(reservationRepository.findByMember_Name("fizz")).thenReturn(reservations);
        List<Reservation> results = reservationService.findByName("fizz");

        assertThat(results.get(0)).isEqualTo(reservations.get(0));
        assertThat(results.get(1)).isEqualTo(reservations.get(1));
    }

    @Test
    void findAllTest() {
        List<Reservation> reservations = List.of(
                new Reservation(1L, fizz, slot),
                new Reservation(2L, luke, otherSlot)
        );

        when(reservationRepository.findAll()).thenReturn(reservations);
        List<Reservation> results = reservationService.findAll();

        assertThat(results.get(0)).isEqualTo(reservations.get(0));
        assertThat(results.get(1)).isEqualTo(reservations.get(1));
    }

    @Test
    void findReservationTest() {
        Reservation reservation = new Reservation(1L, fizz, slot);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThat(reservationService.findReservation(1L)).isEqualTo(reservation);
    }

    @Test
    void findReservationExceptionTest() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findReservation(1L))
                .isInstanceOf(ReservationNotExistsException.class);
    }

    @Test
    void deleteTest() {
        Reservation reservation = new Reservation(1L, fizz, slot);
        reservationService.delete(reservation, false);

        verify(reservationRepository, times(1)).deleteById(1L);
    }

    @Test
    void findBySlotTest() {
        Reservation reservation = new Reservation(1L, fizz, slot);

        when(reservationRepository.findBySlot(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId())).thenReturn(Optional.of(reservation));

        Optional<Reservation> result = reservationService.findBySlot(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isEqualTo(reservation);
    }

    @Test
    void validateReferencedThemeExceptionTest() {
        when(reservationRepository.existsBySlot_Theme_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.validateReferencedTheme(1L))
                .isInstanceOf(CannotDeleteThemeInUseException.class);
    }

    @Test
    void validateReferencedTimeExceptionTest() {
        when(reservationRepository.existsBySlot_Time_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.validateReferencedTime(1L))
                .isInstanceOf(CannotDeleteReservationTimeInUseException.class);
    }
}
