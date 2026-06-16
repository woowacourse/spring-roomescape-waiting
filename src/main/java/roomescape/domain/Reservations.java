package roomescape.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import roomescape.exception.AlreadyExistsException;

public class Reservations {
    private final List<Reservation> values;

    public Reservations(List<Reservation> values) {
        this.values = values;
    }

    public ReservationStatus determineStatus() {
        if (values.stream().anyMatch(Reservation::takesSlot)) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.CONFIRMED;
    }

    public Optional<Reservation> findNextWaiting(Long excludedId) {
        return values.stream()
                .filter(Reservation::isWaiting)
                .filter(r -> !r.getId().equals(excludedId))
                .min(Comparator.comparing(Reservation::getId));
    }

    public List<TimeSlot> toTimeSlots(List<ReservationTime> allTimes) {
        Set<Long> reservedTimeIds = values.stream()
                .filter(Reservation::takesSlot)
                .map(it -> it.getTime().getId())
                .collect(Collectors.toSet());

        return allTimes.stream()
                .map(it -> new TimeSlot(it, getStatus(reservedTimeIds, it.getId())))
                .toList();
    }

    public void validateDuplicate(String name) {
        if (values.stream().anyMatch(r -> r.getName().equals(name))) {
            throw new AlreadyExistsException("이미 예약되었습니다.");
        }
    }

    public Stream<Reservation> stream() {
        return values.stream();
    }

    private ReservationTimeStatus getStatus(Set<Long> reservedTimeIds, Long timeId) {
        if (reservedTimeIds.contains(timeId)) {
            return ReservationTimeStatus.RESERVED;
        }
        return ReservationTimeStatus.AVAILABLE;
    }
}
