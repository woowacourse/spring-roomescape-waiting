package roomescape.domain.reservation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserReservations {

    private final List<ReservationAndWaiting> reservationAndWaitings;

    public UserReservations(String name, List<Reservation> reservations, List<WaitingWithNumber> waitings) {
        validateName(name);
        validateOwner(name, reservations, waitings);
        this.reservationAndWaitings = createUserReservations(reservations, waitings);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }
    }

    private List<ReservationAndWaiting> createUserReservations(List<Reservation> reservations, List<WaitingWithNumber> waitings) {
        List<ReservationAndWaiting> userReservations = new ArrayList<>();

        reservations.stream()
                .map(ReservationAndWaiting::fromReservation)
                .forEach(userReservations::add);

        waitings.stream()
                .map(ReservationAndWaiting::fromWaiting)
                .forEach(userReservations::add);

        sortByDateAndTime(userReservations);
        return List.copyOf(userReservations);
    }

    private void sortByDateAndTime(List<ReservationAndWaiting> userReservations) {
        userReservations.sort(Comparator.comparing(ReservationAndWaiting::date)
                .thenComparing(reservationAndWaiting -> reservationAndWaiting.timeSlot().getStartAt()));
    }

    private void validateOwner(String name, List<Reservation> reservations, List<WaitingWithNumber> waitings) {
        boolean hasDifferentReservation = reservations.stream()
                .anyMatch(reservation -> !reservation.isOwner(name));

        boolean hasDifferentWaiting = waitings.stream()
                .anyMatch(waitingWithNumber -> !waitingWithNumber.waiting().isOwner(name));

        if (hasDifferentReservation || hasDifferentWaiting) {
            throw new IllegalArgumentException("다른 사용자의 예약이나 대기가 포함될 수 없습니다.");
        }
    }

    public List<ReservationAndWaiting> getReservationAndWaitings() {
        return List.copyOf(reservationAndWaitings);
    }
}
