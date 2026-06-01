package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

public class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;

    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        reservationRepository = Mockito.mock(ReservationRepository.class);
        reservationService = new ReservationService(reservationRepository);

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
    }

    @Test
    void saveTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 5, 2),
                reservationTime.getId(), theme.getId());

        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        Reservation reservation = Reservation.of(1L, reservationWithoutId);

        when(reservationRepository.save(reservationWithoutId)).thenReturn(reservation);

        assertThat(reservationService.save(reservationWithoutId)).isEqualTo(reservation);
    }

    @Test
    void findByNameTest() {
        List<Reservation> reservations = List.of(
                new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme),
                new Reservation(2L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme)
        );

        when(reservationRepository.findByName("fizz")).thenReturn(reservations);
        List<Reservation> results = reservationService.findByName("fizz");

        assertThat(results.get(0)).isEqualTo(reservations.get(0));
        assertThat(results.get(1)).isEqualTo(reservations.get(1));
    }

    @Test
    void findAllTest() {
        List<Reservation> reservations = List.of(
                new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme),
                new Reservation(2L, "luke", LocalDate.of(2026, 5, 2), reservationTime, theme)
        );

        when(reservationRepository.findAll()).thenReturn(reservations);
        List<Reservation> results = reservationService.findAll();

        assertThat(results.get(0)).isEqualTo(reservations.get(0));
        assertThat(results.get(1)).isEqualTo(reservations.get(1));
    }

    @Test
    void findReservationTest() {
        Reservation reservation = new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThat(reservationService.findReservation(1L)).isEqualTo(reservation);
    }

    @Test
    void findReservationExceptionTest() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findReservation(1L))
                .isInstanceOf(CustomInvalidRequestException.class);
    }

    @Test
    void deleteTest() {
        reservationService.delete(1L);

        verify(reservationRepository, times(1)).delete(1L);
    }

    @Test
    void findBySlotTest() {
        Reservation reservation = new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme);

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
        doThrow(new CustomInvalidRequestException(ErrorCode.REFERENCED_THEME))
                .when(reservationRepository).existsByThemeId(1L);

        assertThatThrownBy(() -> reservationService.validateReferencedTheme(1L))
                .isInstanceOf(CustomInvalidRequestException.class);
    }

    @Test
    void validateReferencedTimeExceptionTest() {
        doThrow(new CustomInvalidRequestException(ErrorCode.REFERENCED_TIME))
                .when(reservationRepository).existsByTimeId(1L);

        assertThatThrownBy(() -> reservationService.validateReferencedTime(1L))
                .isInstanceOf(CustomInvalidRequestException.class);
    }
}
