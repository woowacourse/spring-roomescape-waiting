package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.domain.Waits;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentService;
import roomescape.payment.PaymentStatus;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.WaitService;
import roomescape.controller.dto.response.ReceptionResponse;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

public class ReceptionFacadeTest {

    private ReservationService reservationService;
    private WaitService waitService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private PaymentService paymentService;
    private Clock clock;
    private ReceptionFacade receptionFacade;

    private LocalDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;
    private LocalDateTime now;

    @BeforeEach
    void beforeEach() {
        reservationService = Mockito.mock(ReservationService.class);
        waitService = Mockito.mock(WaitService.class);
        reservationTimeService = Mockito.mock(ReservationTimeService.class);
        themeService = Mockito.mock(ThemeService.class);
        paymentService = Mockito.mock(PaymentService.class);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));

        reservationDate = LocalDate.of(2026, 5, 3);
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
        now = LocalDateTime.now(clock);

        receptionFacade = new ReceptionFacade(reservationService, waitService, reservationTimeService, themeService,
                paymentService, clock);
    }

    @Test
    void saveReservationTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation pendingReservation = Reservation.pending("fizz", reservationDate, reservationTime, theme,
                "order_test", 50000L);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.lockBySlot(request.reservationDate(), request.timeId(), request.themeId())).thenReturn(
                Optional.empty());
        when(reservationService.savePending(request, reservationTime, theme))
                .thenReturn(pendingReservation);

        assertThat(receptionFacade.save(request)).isEqualTo(
                ReceptionResponse.from(pendingReservation, 0L, ReservationStatus.PENDING.name()));

        InOrder inOrder = Mockito.inOrder(reservationService);
        inOrder.verify(reservationService).deleteStalePendingBefore(now.minusMinutes(10));
        inOrder.verify(reservationService).lockBySlot(request.reservationDate(), request.timeId(), request.themeId());
    }

    @Test
    void confirmPaymentTest() {
        Reservation pendingReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme,
                ReservationStatus.PENDING, "order_test", 50000L, null);
        Reservation confirmedReservation = pendingReservation.confirmPayment("payment_key");

        doNothing().when(reservationService).lockByOrderId("order_test");
        when(reservationService.findByOrderId("order_test")).thenReturn(pendingReservation);
        when(paymentService.confirm("payment_key", "order_test", "order_test", 50000L))
                .thenReturn(new PaymentResult("payment_key", "order_test", "DONE", 50000L));
        when(reservationService.confirmPayment("order_test", "payment_key")).thenReturn(confirmedReservation);

        assertThat(receptionFacade.confirmPayment("payment_key", "order_test", 50000L)).isEqualTo(
                ReceptionResponse.from(confirmedReservation, 0L, ReservationStatus.CONFIRMED.name()));

        InOrder inOrder = Mockito.inOrder(reservationService, paymentService);
        inOrder.verify(reservationService).lockByOrderId("order_test");
        inOrder.verify(reservationService).findByOrderId("order_test");
        inOrder.verify(paymentService).confirm("payment_key", "order_test", "order_test", 50000L);
        inOrder.verify(reservationService).confirmPayment("order_test", "payment_key");
    }

    @Test
    void confirmPaymentAmountMismatchBlocksPaymentGatewayTest() {
        Reservation pendingReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme,
                ReservationStatus.PENDING, "order_test", 50000L, null);

        when(reservationService.findByOrderId("order_test")).thenReturn(pendingReservation);

        assertThatThrownBy(() -> receptionFacade.confirmPayment("payment_key", "order_test", 1000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH));
        verify(paymentService, never()).confirm(any(), any(), any(), any());
    }

    @Test
    void confirmPaymentGatewayResultMismatchExceptionTest() {
        Reservation pendingReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme,
                ReservationStatus.PENDING, "order_test", 50000L, null);

        when(reservationService.findByOrderId("order_test")).thenReturn(pendingReservation);
        when(paymentService.confirm("payment_key", "order_test", "order_test", 50000L))
                .thenReturn(new PaymentResult("payment_key", "other_order", "DONE", 50000L));

        assertThatThrownBy(() -> receptionFacade.confirmPayment("payment_key", "order_test", 50000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_FAILED));
        verify(reservationService, never()).confirmPayment(any(), any());
    }

    @Test
    void confirmPaymentGatewayStatusNotDoneExceptionTest() {
        Reservation pendingReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme,
                ReservationStatus.PENDING, "order_test", 50000L, null);

        when(reservationService.findByOrderId("order_test")).thenReturn(pendingReservation);
        when(paymentService.confirm("payment_key", "order_test", "order_test", 50000L))
                .thenReturn(new PaymentResult("payment_key", "order_test", "ABORTED", 50000L));

        assertThatThrownBy(() -> receptionFacade.confirmPayment("payment_key", "order_test", 50000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_FAILED));
        verify(reservationService, never()).confirmPayment(any(), any());
    }

    @Test
    void failPaymentWithNullOrderIdDoesNotThrowTest() {
        receptionFacade.failPayment(null);

        verify(reservationService, times(0)).deletePendingByOrderId(any());
    }

    @Test
    void failPaymentDeletesPendingReservationTest() {
        receptionFacade.failPayment("order_test");

        verify(reservationService, times(1)).deletePendingByOrderId("order_test");
    }

    @Test
    void saveWaitTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, "luke", reservationDate, reservationTime, theme);

        Wait newWait = request.toWait(now, reservationTime, theme);
        Wait savedWait = Wait.of(1L, newWait);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.lockBySlot(request.reservationDate(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation.getId()));
        when(reservationService.findReservation(beforeReservation.getId())).thenReturn(beforeReservation);
        when(waitService.save(newWait)).thenReturn(savedWait);
        when(waitService.calculateOrder(savedWait)).thenReturn(1L);

        assertThat(receptionFacade.save(request)).isEqualTo(
                ReceptionResponse.from(savedWait, 1L, ReservationStatus.WAITING.name()));

        verify(reservationService, times(1)).deleteStalePendingBefore(now.minusMinutes(10));
    }

    @Test
    void savePastTimeReservationCreateExceptionTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", LocalDate.of(2026, 3, 20),
                reservationTime.getId(), theme.getId());

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);

        assertThatThrownBy(() -> receptionFacade.save(request))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.PAST_RESERVATION_CREATE));
    }

    @Test
    void saveDuplicateReservationExceptionTest() {
        ServiceReservationCreateRequest request = new ServiceReservationCreateRequest("fizz", reservationDate,
                reservationTime.getId(), theme.getId());
        Reservation beforeReservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);

        when(reservationTimeService.findReservationTime(reservationTime.getId())).thenReturn(reservationTime);
        when(themeService.findTheme(theme.getId())).thenReturn(theme);
        when(reservationService.lockBySlot(request.reservationDate(), request.timeId(), request.themeId())).thenReturn(
                Optional.of(beforeReservation.getId()));
        when(reservationService.findReservation(beforeReservation.getId())).thenReturn(beforeReservation);

        assertThatThrownBy(() -> receptionFacade.save(request))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.DUPLICATED_RESERVATION));
    }

    @Test
    void findByNameTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        Reservation reservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);
        Wait wait = new Wait(1L, now, "fizz", otherDate, reservationTime, theme);

        when(reservationService.findByName("fizz")).thenReturn(List.of(reservation));
        when(waitService.findByName("fizz")).thenReturn(List.of(wait));
        when(waitService.calculateOrder(wait)).thenReturn(1L);

        List<ReceptionResponse> result = List.of(
                ReceptionResponse.from(reservation, 0L, ReservationStatus.CONFIRMED.name()),
                ReceptionResponse.from(wait, 1L, ReservationStatus.WAITING.name())
        );

        assertThat(receptionFacade.findByName("fizz")).isEqualTo(result);
    }

    @Test
    void findAllTest() {
        LocalDate otherDate = LocalDate.of(2026, 5, 19);

        Reservation reservation1 = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);
        Reservation reservation2 = new Reservation(2L, "luke", otherDate, reservationTime, theme);
        Wait wait1 = new Wait(1L, now, "fizz", otherDate, reservationTime, theme);
        Wait wait2 = new Wait(2L, now, "luke", reservationDate, reservationTime, theme);

        when(reservationService.findAll()).thenReturn(List.of(reservation1, reservation2));
        when(waitService.findAll()).thenReturn(List.of(wait1, wait2));
        when(waitService.calculateOrder(wait1)).thenReturn(1L);
        when(waitService.calculateOrder(wait2)).thenReturn(1L);

        List<ReceptionResponse> result = List.of(
                ReceptionResponse.from(reservation1, 0L, ReservationStatus.CONFIRMED.name()),
                ReceptionResponse.from(reservation2, 0L, ReservationStatus.CONFIRMED.name()),
                ReceptionResponse.from(wait1, 1L, ReservationStatus.WAITING.name()),
                ReceptionResponse.from(wait2, 1L, ReservationStatus.WAITING.name())
        );

        assertThat(receptionFacade.findAll()).isEqualTo(result);
    }

    @Test
    void deleteReservationWithoutWaitTest() {
        Reservation reservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);

        doNothing().when(reservationService).lockById(reservation.getId());
        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())).thenReturn(new Waits(List.of()));

        receptionFacade.deleteReservation(reservation.getId());

        verify(reservationService, times(1)).delete(reservation.getId());
    }

    @Test
    void deleteReservationWithWaitTest() {
        Reservation reservation = new Reservation(1L, "fizz", reservationDate, reservationTime, theme);
        Wait firstWait = new Wait(1L, now, "fizz", reservationDate, reservationTime, theme);
        ServiceReservationCreateRequest waitRequest = new ServiceReservationCreateRequest(firstWait.getName(),
                firstWait.getReservationDate(),
                firstWait.getTime().getId(), firstWait.getTheme().getId());

        doNothing().when(reservationService).lockById(reservation.getId());
        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);
        when(waitService.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())).thenReturn(new Waits(List.of(firstWait)));

        receptionFacade.deleteReservation(reservation.getId());
        verify(reservationService, times(1)).save(waitRequest, firstWait.getTime(), firstWait.getTheme());
        verify(reservationService, times(1)).delete(reservation.getId());
        verify(waitService, times(1)).delete(firstWait.getId());
    }

    @Test
    void deleteReservationExceptionTest() {
        LocalDate pastReservationDate = LocalDate.of(2026, 3, 20);
        Reservation reservation = new Reservation(1L, "fizz", pastReservationDate, reservationTime, theme);

        doNothing().when(reservationService).lockById(reservation.getId());
        when(reservationService.findReservation(reservation.getId())).thenReturn(reservation);

        assertThatThrownBy(() -> receptionFacade.deleteReservation(reservation.getId()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.PAST_RESERVATION_DELETE));
    }

    @Test
    void deleteWaitTest() {
        Wait wait = new Wait(1L, now, "fizz", reservationDate, reservationTime, theme);

        when(waitService.findWait(wait.getId())).thenReturn(wait);

        receptionFacade.deleteWait(wait.getId());

        verify(waitService, times(1)).delete(wait.getId());
    }
}
