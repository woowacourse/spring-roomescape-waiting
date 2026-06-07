package roomescape.reservation.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Reservations {
    private static final int RESERVED_RESERVATION_INDEX = 0;

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        validate(reservations);

        this.reservations = List.copyOf(reservations);
    }

    public List<ReservationEntry> entries() {
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        Reservation::getSlot,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .flatMap(sameSlotReservations -> entriesInSameSlot(sameSlotReservations).stream())
                .toList();
    }

    private List<ReservationEntry> entriesInSameSlot(List<Reservation> reservations) {
        return IntStream.range(RESERVED_RESERVATION_INDEX, reservations.size())
                .mapToObj(index -> entry(reservations.get(index), index))
                .toList();
    }

    private ReservationEntry entry(Reservation reservation, int index) {
        if (index == RESERVED_RESERVATION_INDEX) {
            return ReservationEntry.reserved(reservation);
        }

        return ReservationEntry.waiting(reservation, index);
    }

    private static void validate(List<Reservation> reservations) {
        if (reservations == null) {
            throw new IllegalArgumentException("예약 목록은 비어 있을 수 없습니다.");
        }
        if (reservations.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("예약 목록에는 빈 예약이 포함될 수 없습니다.");
        }
    }
}
