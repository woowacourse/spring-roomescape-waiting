package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.payment.OrderIdGenerator;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

public class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    private OrderIdGenerator orderIdGenerator;

    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        reservationRepository = Mockito.mock(ReservationRepository.class);
        orderIdGenerator = Mockito.mock(OrderIdGenerator.class);
        reservationService = new ReservationService(reservationRepository, orderIdGenerator);

        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
    }

    @Test
    void savePendingTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 5, 2),
                reservationTime.getId(), theme.getId());

        when(orderIdGenerator.generate()).thenReturn("order_test");
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> Reservation.of(1L, invocation.getArgument(0)));

        Reservation result = reservationService.savePending(request, reservationTime, theme);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation pendingReservation = captor.getValue();
        assertThat(pendingReservation.getOrderId()).isEqualTo("order_test");
        assertThat(pendingReservation.getIdempotencyKey()).isNotBlank();
        assertThat(pendingReservation.getIdempotencyKey()).isNotEqualTo("order_test");
        assertThat(result).isEqualTo(Reservation.of(1L, pendingReservation));
    }

    @Test
    void saveTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 5, 2),
                reservationTime.getId(), theme.getId());

        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        Reservation reservation = Reservation.of(1L, reservationWithoutId);

        when(reservationRepository.save(reservationWithoutId)).thenReturn(reservation);
        Reservation result = reservationService.save(request, reservationTime, theme);

        assertThat(result).isEqualTo(reservation);
    }

    @Test
    void deleteStalePendingBeforeTest() {
        LocalDateTime expiresBefore = LocalDateTime.of(2026, 5, 2, 8, 50);

        reservationService.deleteStalePendingBefore(expiresBefore);

        verify(reservationRepository, times(1)).deleteStalePendingBefore(expiresBefore);
    }

    @Test
    void findByNameTest() {
        List<Reservation> reservations = List.of(
                new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme),
                new Reservation(2L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme)
        );

        when(reservationRepository.findByName("fizz")).thenReturn(reservations);

        assertThat(reservationService.findByName("fizz")).isEqualTo(reservations);
    }

    @Test
    void findAllTest() {
        List<Reservation> reservations = List.of(
                new Reservation(1L, "fizz", LocalDate.of(2026, 5, 2), reservationTime, theme),
                new Reservation(2L, "luke", LocalDate.of(2026, 5, 2), reservationTime, theme)
        );

        when(reservationRepository.findAll()).thenReturn(reservations);

        assertThat(reservationService.findAll()).isEqualTo(reservations);
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
                .isInstanceOf(RoomEscapeException.class);
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
        doThrow(new RoomEscapeException(DomainErrorCode.REFERENCED_THEME))
                .when(reservationRepository).existsByThemeId(1L);

        assertThatThrownBy(() -> reservationService.validateReferencedTheme(1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void validateReferencedTimeExceptionTest() {
        doThrow(new RoomEscapeException(DomainErrorCode.REFERENCED_TIME))
                .when(reservationRepository).existsByTimeId(1L);

        assertThatThrownBy(() -> reservationService.validateReferencedTime(1L))
                .isInstanceOf(RoomEscapeException.class);
    }
}
