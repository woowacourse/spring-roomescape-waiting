package roomescape.domain;

import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
    }

    public void validateDuplicate(String name) {
        boolean exists = reservations.stream()
                .anyMatch(r -> r.getName().equals(name) && !r.isCancelled());
        if (exists) throw new CustomException(ErrorCode.RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT);
    }

    public boolean hasNoActiveReservation() {
        return reservations.stream().allMatch(Reservation::isCancelled);
    }

    public Optional<Reservation> findFirstPending() {
        return reservations.stream()
                .filter(Reservation::isPending)
                .min(Comparator.comparing(Reservation::getId));
    }

    public int waitingOrderOf(Long reservationId) {
        List<Reservation> pending = reservations.stream()
                .filter(Reservation::isPending)
                .sorted(Comparator.comparing(Reservation::getId))
                .toList();
        for (int i = 0; i < pending.size(); i++) {
            if (pending.get(i).getId().equals(reservationId)) return i + 1;
        }
        throw new IllegalArgumentException("대기 예약이 존재하지 않습니다.");
    }

    public List<Reservation> pendingByOrder() {
        return reservations.stream()
                .filter(Reservation::isPending)
                .sorted(Comparator.comparing(Reservation::getId))
                .toList();
    }

    public Reservation findById(Long id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public void add(Reservation reservation) {
        reservations.add(reservation);
    }

    public List<Reservation> toList() {
        return Collections.unmodifiableList(reservations);
    }
}
