package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationslot.ReservationSlot;

public class ReservationWaitingLine {
    public static final String UNSAVED_WAITING_MESSAGE = "저장되지 않은 대기는 대기 줄에 포함될 수 없습니다.";
    public static final String DIFFERENT_SLOT_MESSAGE = "서로 다른 슬롯의 대기를 하나의 대기 줄로 묶을 수 없습니다.";
    private static final Comparator<ReservationWaitingOrder> WAITING_ORDER = Comparator
            .comparing(ReservationWaitingOrder::requestedAt)
            .thenComparing(ReservationWaitingOrder::waitingId);

    private final List<ReservationWaiting> waitings;

    public ReservationWaitingLine(final List<ReservationWaiting> waitings) {
        validate(waitings);
        this.waitings = waitings.stream()
                .sorted((first, second) -> WAITING_ORDER.compare(toOrder(first), toOrder(second)))
                .toList();
    }

    public static ReservationWaitingLine fromWaitings(final List<ReservationWaiting> waitings) {
        return new ReservationWaitingLine(waitings);
    }

    public boolean isEmpty() {
        return waitings.isEmpty();
    }

    public boolean containsName(final ReservationName name) {
        return waitings.stream()
                .map(ReservationWaiting::getName)
                .anyMatch(name.value()::equals);
    }

    public Optional<ReservationWaiting> first() {
        return waitings.stream()
                .findFirst();
    }

    public OptionalInt indexOf(final long waitingId) {
        return indexOf(waitings.stream()
                .map(ReservationWaitingLine::toOrder)
                .toList(), waitingId);
    }

    public static OptionalInt indexOf(final List<ReservationWaitingOrder> orders, final long waitingId) {
        validateOrders(orders);
        List<ReservationWaitingOrder> sortedOrders = orders.stream()
                .sorted(WAITING_ORDER)
                .toList();

        for (int index = 0; index < sortedOrders.size(); index++) {
            if (sortedOrders.get(index).waitingId() == waitingId) {
                return OptionalInt.of(index);
            }
        }

        return OptionalInt.empty();
    }

    private void validate(final List<ReservationWaiting> waitings) {
        validateOrders(waitings.stream()
                .map(ReservationWaitingLine::toOrder)
                .toList());
    }

    private static void validateOrders(final List<ReservationWaitingOrder> orders) {
        if (orders.isEmpty()) {
            return;
        }

        Long slotId = orders.get(0).slotId();
        for (ReservationWaitingOrder order : orders) {
            validateSaved(order);
            validateSameSlot(slotId, order);
        }
    }

    private static void validateSaved(final ReservationWaitingOrder order) {
        if (order.waitingId() == null || order.slotId() == null) {
            throw new IllegalArgumentException(UNSAVED_WAITING_MESSAGE);
        }
    }

    private static void validateSameSlot(final Long slotId, final ReservationWaitingOrder order) {
        if (!Objects.equals(slotId, order.slotId())) {
            throw new IllegalArgumentException(DIFFERENT_SLOT_MESSAGE);
        }
    }

    private static ReservationWaitingOrder toOrder(final ReservationWaiting waiting) {
        ReservationSlot slot = waiting.getSlot();
        return new ReservationWaitingOrder(waiting.getId(), slot.getId(), waiting.getRequestedAt());
    }

    public record ReservationWaitingOrder(
            Long waitingId,
            Long slotId,
            LocalDateTime requestedAt
    ) {
    }
}
