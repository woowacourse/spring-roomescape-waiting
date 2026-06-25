package roomescape.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.PaymentRepository;
import roomescape.service.dto.BookingStatus;

@Service
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class BookingLookupService {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;
    private final PaymentRepository paymentRepository;

    public BookingLookupService(ReservationService reservationService,
                                ReservationWaitingService reservationWaitingService,
                                PaymentRepository paymentRepository) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
        this.paymentRepository = paymentRepository;
    }

    public List<BookingStatus> findByName(String name) {
        return Stream.concat(
                        reservationService.findByName(name).stream()
                                .map(reservation -> BookingStatus.reservation(
                                        reservation,
                                        paymentRepository.findLatestByReservationId(reservation.getId()).orElse(null)
                                )),
                        reservationWaitingService.findByName(name).stream()
                                .map(BookingStatus::waiting))
                .sorted(Comparator
                        .comparing(BookingStatus::date, Comparator.reverseOrder())
                        .thenComparing(status -> status.time().getStartAt(), Comparator.reverseOrder()))
                .toList();
    }

    public List<BookingStatus> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RoomescapeException(ErrorCode.INVALID_INPUT, "시작일은 종료일보다 늦을 수 없습니다.");
        }
        return Stream.concat(
                        reservationService.findByDateRange(startDate, endDate).stream()
                                .map(reservation -> BookingStatus.reservation(
                                        reservation,
                                        paymentRepository.findLatestByReservationId(reservation.getId()).orElse(null)
                                )),
                        reservationWaitingService.findByDateRange(startDate, endDate).stream()
                                .map(BookingStatus::waiting))
                .sorted(Comparator
                        .comparing(BookingStatus::date)
                        .thenComparing(status -> status.time().getStartAt())
                        .thenComparing(status -> status.theme().getName())
                        .thenComparing(BookingStatus::bookingType)
                        .thenComparing(BookingStatus::turn, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
    }
}
