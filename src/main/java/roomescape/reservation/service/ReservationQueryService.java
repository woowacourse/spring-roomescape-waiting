package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.domain.Payment;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.query.dto.ReservationWithStatusResult;
import roomescape.reservation.repository.ReservationQueryDao;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private static final Comparator<ReservationWithStatusResult> RESERVATION_WITH_STATUS_RESULT_COMPARATOR =
            Comparator.comparingInt((ReservationWithStatusResult result) -> result.status().priority())
                    .thenComparing(ReservationWithStatusResult::date)
                    .thenComparing(result -> result.time().getStartAt())
                    .thenComparing(ReservationWithStatusResult::waitingOrder);

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationQueryDao reservationQueryDao;
    private final PaymentRepository paymentRepository;

    public ReservationQueryService(ReservationRepository reservationRepository,
                                   ReservationWaitingRepository reservationWaitingRepository,
                                   ReservationQueryDao reservationQueryDao,
                                   PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationQueryDao = reservationQueryDao;
        this.paymentRepository = paymentRepository;
    }

    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationWithStatusResult> findAllByName(String name) {
        List<ReservationWaiting> waitings = reservationWaitingRepository.findAllByName(name);
        return combineAndSort(getReservationResults(name), getReservationWaitingResults(waitings));
    }

    public PopularThemesResult queryPopularThemes(int period, int limit) {
        int oneDayDifference = 1;
        LocalDate to = LocalDate.now().minusDays(oneDayDifference);
        LocalDate from = to.minusDays(period).plusDays(oneDayDifference);
        return new PopularThemesResult(
                reservationQueryDao.queryPopularThemes(from, to, limit)
        );
    }

    private List<ReservationWithStatusResult> getReservationResults(String name) {
        return reservationRepository.findAllByName(name).stream()
                .map(this::toResultWithOrderId)
                .toList();
    }

    private ReservationWithStatusResult toResultWithOrderId(Reservation reservation) {
        if (reservation.isConfirmed()) {
            return ReservationWithStatusResult.from(reservation);
        }
        String orderId = paymentRepository.findByReservationId(reservation.getId())
                .map(Payment::getOrderId)
                .orElse(null);
        return ReservationWithStatusResult.from(reservation, orderId);
    }

    private List<ReservationWithStatusResult> getReservationWaitingResults(List<ReservationWaiting> waitings) {
        List<ReservationSlot> slots = extractSlotFromWaitings(waitings);
        Map<ReservationSlot, List<ReservationWaiting>> waitingsPerSlot = findReservationWaitingsPerSlot(slots);

        return waitings.stream()
                .map(waiting -> {
                    ReservationSlot currentSlot = waiting.getSlot();
                    int rank = waitingsPerSlot.get(currentSlot).indexOf(waiting) + 1;
                    return ReservationWithStatusResult.from(waiting, rank);
                })
                .toList();
    }

    private Map<ReservationSlot, List<ReservationWaiting>> findReservationWaitingsPerSlot(List<ReservationSlot> slots) {
        Map<ReservationSlot, List<ReservationWaiting>> grouped = reservationWaitingRepository.findAllBySlots(slots)
                .stream()
                .collect(Collectors.groupingBy(ReservationWaiting::getSlot));
        return sortWaitingQueues(slots, grouped);
    }

    private Map<ReservationSlot, List<ReservationWaiting>> sortWaitingQueues(
            List<ReservationSlot> slots, Map<ReservationSlot, List<ReservationWaiting>> grouped
    ) {
        return slots.stream()
                .collect(Collectors.toUnmodifiableMap(
                        slot -> slot,
                        slot -> grouped.getOrDefault(slot, List.of()).stream()
                                .sorted()
                                .toList()
                ));
    }

    private List<ReservationSlot> extractSlotFromWaitings(List<ReservationWaiting> waitings) {
        return waitings.stream()
                .map(ReservationWaiting::getSlot)
                .toList();
    }

    private List<ReservationWithStatusResult> combineAndSort(
            List<ReservationWithStatusResult> reservations, List<ReservationWithStatusResult> waitings
    ) {
        List<ReservationWithStatusResult> combined = new ArrayList<>();
        if (reservations != null) {
            combined.addAll(reservations);
        }
        if (waitings != null) {
            combined.addAll(waitings);
        }

        combined.sort(RESERVATION_WITH_STATUS_RESULT_COMPARATOR);
        return combined;
    }

}

