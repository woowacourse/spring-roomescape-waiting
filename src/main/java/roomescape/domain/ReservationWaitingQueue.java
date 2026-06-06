package roomescape.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReservationWaitingQueue {
    private static final Comparator<ReservationWaiting> WAITING_ORDER =
            Comparator.comparing(ReservationWaiting::getCreatedAt)
                    .thenComparing(ReservationWaiting::getId);

    private final List<ReservationWaiting> waitings;

    public ReservationWaitingQueue(List<ReservationWaiting> waitings) {
        Objects.requireNonNull(waitings, "예약 대기 목록은 필수값 입니다.");
        this.waitings = waitings.stream()
                .sorted(WAITING_ORDER)
                .toList();
    }

    public int orderOf(ReservationWaiting waiting) {
        Objects.requireNonNull(waiting, "예약 대기는 필수값 입니다.");

        int index = waitings.indexOf(waiting);
        if (index < 0) {
            throw new IllegalArgumentException("대기열에 존재하지 않는 예약입니다.");
        }

        return index + 1;
    }

    public Optional<ReservationWaiting> first() {
        if (waitings.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(waitings.getFirst());
    }
}
