package roomescape.domain.reserveticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import roomescape.domain.reservation.Reservation;
import roomescape.service.reserveticket.ReservationWithWaitingRank;

public class ReservationWithWaitingRanks {

    private final List<ReservationWithWaitingRank> reservationWithRanks;

    public ReservationWithWaitingRanks(List<Reservation> reserveTickets) {
        Map<Reservation, List<Reservation>> reservationsMap = new HashMap<>();

        for (Reservation reservation : reserveTickets) {
            reservationsMap.putIfAbsent(reservation, new ArrayList<>());

            List<Reservation> reservations = reservationsMap.getOrDefault(reservation, new ArrayList<>());
            reservations.add(reservation);
            reservationsMap.put(reservation, reservations);
        }
        this.reservationWithRanks = calculateReservationWaitingRank(reservationsMap);
    }

    private List<ReservationWithWaitingRank> calculateReservationWaitingRank(
            Map<Reservation, List<Reservation>> reservationsMap) {
        List<ReservationWithWaitingRank> reservationWithWaitingRanks = new ArrayList<>();
        for (Reservation compareObjectReservation : reservationsMap.keySet()) {
            List<Reservation> reservations = reservationsMap.get(compareObjectReservation);
            List<ReservationWithWaitingRank> createdReservationWaitings = createReservationWaitingListFromReservation(
                    reservations);
            reservationWithWaitingRanks.addAll(createdReservationWaitings);
        }
        return reservationWithWaitingRanks;
    }

    private List<ReservationWithWaitingRank> createReservationWaitingListFromReservation(
            List<Reservation> reservations) {

        List<ReservationWithWaitingRank> reservationWithWaitingRank = new ArrayList<>();

        for (int waitRank = 1; waitRank <= reservations.size(); waitRank++) {
            Reservation reservation = reservations.get(waitRank - 1);
            reservationWithWaitingRank.add(
                    new ReservationWithWaitingRank(reservation.getId(), reservation.getDate(), reservation.getStartAt(),
                            reservation.getReservationStatus(), waitRank, reservation.getThemeName()));
        }

        return reservationWithWaitingRank;
    }

    public List<ReservationWithWaitingRank> getReservationWithRanks() {
        return Collections.unmodifiableList(reservationWithRanks);
    }
}
