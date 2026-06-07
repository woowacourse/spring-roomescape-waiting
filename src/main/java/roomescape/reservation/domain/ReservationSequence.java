package roomescape.reservation.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReservationSequence {
    private static final int FIRST_WAITING_RESERVATION_INDEX = 1;

    private final Slot slot;
    private final List<Reservation> reservations;

    public ReservationSequence(Slot slot, List<Reservation> reservations) {
        validate(slot, reservations);

        this.slot = slot;
        this.reservations = List.copyOf(reservations);
    }

    public static List<ReservationEntry> entriesOf(List<Reservation> reservations) {
        validateReservations(reservations);

        return reservations.stream()
                .collect(Collectors.groupingBy(
                        Reservation::getSlot,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .flatMap(entry -> new ReservationSequence(entry.getKey(), entry.getValue()).entries().stream())
                .toList();
    }

    public List<ReservationEntry> entries() {
        if (reservations.isEmpty()) {
            return List.of();
        }

        List<ReservationEntry> entries = new ArrayList<>();
        entries.add(ReservationEntry.reserved(reservations.getFirst()));
        entries.addAll(waitingEntries());

        return List.copyOf(entries);
    }

    private List<ReservationEntry> waitingEntries() {
        return IntStream.range(FIRST_WAITING_RESERVATION_INDEX, reservations.size())
                .mapToObj(this::waitingEntry)
                .toList();
    }

    private ReservationEntry waitingEntry(int reservationIndex) {
        Reservation reservation = reservations.get(reservationIndex);
        return ReservationEntry.waiting(reservation, reservationIndex);
    }

    private static void validate(Slot slot, List<Reservation> reservations) {
        validateSlot(slot);
        validateReservations(reservations);
        validateSameSlot(slot, reservations);
    }

    private static void validateSlot(Slot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }

    private static void validateReservations(List<Reservation> reservations) {
        if (reservations == null) {
            throw new IllegalArgumentException("예약 목록은 비어 있을 수 없습니다.");
        }
        if (reservations.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("예약 목록에는 빈 예약이 포함될 수 없습니다.");
        }
    }

    private static void validateSameSlot(Slot slot, List<Reservation> reservations) {
        if (reservations.stream().anyMatch(reservation -> !slot.contains(reservation))) {
            throw new IllegalArgumentException("서로 다른 슬롯의 예약은 하나의 순서로 묶을 수 없습니다.");
        }
    }
}
