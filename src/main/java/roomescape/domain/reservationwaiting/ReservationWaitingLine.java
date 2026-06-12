package roomescape.domain.reservationwaiting;

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

    private final List<ReservationWaiting> waitings;

    public ReservationWaitingLine(final List<ReservationWaiting> waitings) {
        validate(waitings);
        this.waitings = waitings.stream()
                .sorted(Comparator.comparing(ReservationWaiting::getRequestedAt)
                        .thenComparing(ReservationWaiting::getId))
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
        for (int index = 0; index < waitings.size(); index++) {
            if (waitings.get(index).getId() == waitingId) {
                return OptionalInt.of(index);
            }
        }

        return OptionalInt.empty();
    }

    private void validate(final List<ReservationWaiting> waitings) {
        if (waitings.isEmpty()) {
            return;
        }

        Long slotId = waitingSlotId(waitings.get(0));
        for (ReservationWaiting waiting : waitings) {
            validateSaved(waiting);
            validateSameSlot(slotId, waiting);
        }
    }

    private void validateSaved(final ReservationWaiting waiting) {
        if (waiting.getId() == null || waitingSlotId(waiting) == null) {
            throw new IllegalArgumentException(UNSAVED_WAITING_MESSAGE);
        }
    }

    private void validateSameSlot(final Long slotId, final ReservationWaiting waiting) {
        if (!Objects.equals(slotId, waitingSlotId(waiting))) {
            throw new IllegalArgumentException(DIFFERENT_SLOT_MESSAGE);
        }
    }

    private Long waitingSlotId(final ReservationWaiting waiting) {
        ReservationSlot slot = waiting.getSlot();
        return slot.getId();
    }
}
