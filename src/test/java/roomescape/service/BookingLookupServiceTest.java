package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;
import roomescape.domain.WaitingWithTurn;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.PaymentRepository;
import roomescape.service.dto.BookingStatus;
import roomescape.service.dto.BookingType;

class BookingLookupServiceTest {

    private final ReservationService reservationService = mock();
    private final ReservationWaitingService reservationWaitingService = mock();
    private final PaymentRepository paymentRepository = mock();
    private final BookingLookupService service = new BookingLookupService(
            reservationService,
            reservationWaitingService,
            paymentRepository);

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");

    @Test
    void 이름으로_예약과_예약_대기를_함께_조회한다() {
        String name = "브라운";
        Reservation reservation = new Reservation(1L, new Reserver(name), new ReservationSlot(date, time, theme));
        WaitingWithTurn waiting = new WaitingWithTurn(
                new ReservationWaiting(2L, new Reserver(name), new ReservationSlot(date.plusDays(1), time, theme)),
                1L);

        when(reservationService.findByName(name)).thenReturn(List.of(reservation));
        when(reservationWaitingService.findByName(name)).thenReturn(List.of(waiting));
        when(paymentRepository.findLatestByReservationId(1L)).thenReturn(java.util.Optional.of(
                Payment.restore(1L, 1L, "payment_ready_123456789012345678901", 20_000L, null,
                        PaymentStatus.READY, null, null)
        ));

        List<BookingStatus> result = service.findByName(name);

        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(BookingStatus::id).containsExactly(2L, 1L),
                () -> assertThat(result).extracting(BookingStatus::bookingType)
                        .containsExactly(BookingType.WAITING, BookingType.RESERVATION),
                () -> assertThat(result).extracting(BookingStatus::reservationStatus)
                        .containsExactly(null, ReservationStatus.CONFIRMED),
                () -> assertThat(result).extracting(BookingStatus::turn).containsExactly(1L, null),
                () -> assertThat(result.get(1).payment().orderId()).isEqualTo("payment_ready_123456789012345678901"),
                () -> assertThat(result.get(1).payment().status()).isEqualTo(PaymentStatus.READY),
                () -> assertThat(result.get(0).payment()).isNull());
    }

    @Test
    void 예약과_예약_대기를_날짜와_시간_내림차순으로_조회한다() {
        String name = "브라운";
        ReservationTime earlyTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        ReservationTime lateTime = new ReservationTime(2L, LocalTime.parse("12:00"));

        Reservation earlyReservation = new Reservation(1L, new Reserver(name), new ReservationSlot(date, earlyTime, theme));
        Reservation lateReservation = new Reservation(2L, new Reserver(name), new ReservationSlot(date, lateTime, theme));
        WaitingWithTurn futureWaiting = new WaitingWithTurn(
                new ReservationWaiting(3L, new Reserver(name), new ReservationSlot(date.plusDays(1), earlyTime, theme)),
                1L);

        when(reservationService.findByName(name)).thenReturn(List.of(earlyReservation, lateReservation));
        when(reservationWaitingService.findByName(name)).thenReturn(List.of(futureWaiting));

        List<BookingStatus> result = service.findByName(name);

        assertThat(result).extracting(BookingStatus::id).containsExactly(3L, 2L, 1L);
    }

    @Test
    void 기간으로_예약과_예약_대기를_날짜_시간_테마_유형_순번순으로_정렬한다() {
        LocalDate startDate = date.minusDays(1);
        LocalDate endDate = date.plusDays(1);
        ReservationTime sameTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        ReservationTime lateTime = new ReservationTime(2L, LocalTime.parse("12:00"));
        Theme theme1 = new Theme(1L, "테마1", "테마 설명", "썸네일 주소");
        Theme theme3 = new Theme(3L, "테마3", "테마 설명", "썸네일 주소");

        Reservation previousDateReservation = new Reservation(
                4L, new Reserver("아로"), new ReservationSlot(date.minusDays(1), lateTime, theme1));
        Reservation theme1Reservation = new Reservation(1L, new Reserver("브라운"), new ReservationSlot(date, sameTime, theme1));
        Reservation lateReservation = new Reservation(5L, new Reserver("포비"), new ReservationSlot(date, lateTime, theme1));
        WaitingWithTurn theme3Waiting = new WaitingWithTurn(
                new ReservationWaiting(3L, new Reserver("구구"), new ReservationSlot(date, sameTime, theme3)),
                1L);
        WaitingWithTurn theme1Waiting = new WaitingWithTurn(
                new ReservationWaiting(2L, new Reserver("도라"), new ReservationSlot(date, sameTime, theme1)),
                1L);

        when(reservationService.findByDateRange(startDate, endDate))
                .thenReturn(List.of(lateReservation, theme1Reservation, previousDateReservation));
        when(reservationWaitingService.findByDateRange(startDate, endDate))
                .thenReturn(List.of(theme3Waiting, theme1Waiting));

        List<BookingStatus> result = service.findByDateRange(startDate, endDate);

        assertAll(
                () -> assertThat(result).extracting(BookingStatus::id).containsExactly(4L, 1L, 2L, 3L, 5L),
                () -> assertThat(result).extracting(BookingStatus::bookingType)
                        .containsExactly(BookingType.RESERVATION, BookingType.RESERVATION, BookingType.WAITING,
                                BookingType.WAITING, BookingType.RESERVATION),
                () -> assertThat(result).extracting(BookingStatus::reservationStatus)
                        .containsExactly(ReservationStatus.CONFIRMED, ReservationStatus.CONFIRMED, null, null,
                                ReservationStatus.CONFIRMED),
                () -> assertThat(result).extracting(status -> status.theme().getName())
                        .containsExactly("테마1", "테마1", "테마1", "테마3", "테마1"));
    }

    @Test
    void 시작일이_종료일보다_늦으면_예외가_발생한다() {
        LocalDate startDate = date.plusDays(1);
        LocalDate endDate = date;

        assertThatThrownBy(() -> service.findByDateRange(startDate, endDate))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT)
                .hasMessage("시작일은 종료일보다 늦을 수 없습니다.");
    }
}
