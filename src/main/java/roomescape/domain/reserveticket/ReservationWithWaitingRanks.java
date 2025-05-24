package roomescape.domain.reserveticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import roomescape.domain.reservation.Reservation;

public class ReservationWithWaitingRanks {

    private final List<ReservationWithWaitingRank> reservationWithRanks;

    public ReservationWithWaitingRanks(List<Reservation> reserveTickets) {
        List<ReservationWithWaitingRank> reservationWithWaitingRankList = reserveTickets.stream()
                .map(ReservationWithWaitingRank::new)
                .toList();

        Map<ReservationWithWaitingRank, List<ReservationWithWaitingRank>> reservationsMap = new HashMap<>();

        for (ReservationWithWaitingRank reservationWithRank : reservationWithWaitingRankList) {
            reservationsMap.putIfAbsent(reservationWithRank, new ArrayList<>());

            List<ReservationWithWaitingRank> reservations = reservationsMap.getOrDefault(reservationWithRank,
                    new ArrayList<>());
            reservations.add(reservationWithRank);
            reservationsMap.put(reservationWithRank, reservations);
        }
        this.reservationWithRanks = calculateReservationWaitingRank(reservationsMap);
    }

    private List<ReservationWithWaitingRank> calculateReservationWaitingRank(
            Map<ReservationWithWaitingRank, List<ReservationWithWaitingRank>> reservationsMap) {
        List<ReservationWithWaitingRank> reservationWithWaitingRanks = new ArrayList<>();
        for (ReservationWithWaitingRank compareObjectReservation : reservationsMap.keySet()) {
            List<ReservationWithWaitingRank> reservations = reservationsMap.get(compareObjectReservation);
            List<ReservationWithWaitingRank> createdReservationWaitings = createReservationWaitingListFromReservation(
                    reservations);
            reservationWithWaitingRanks.addAll(createdReservationWaitings);
        }
        return reservationWithWaitingRanks;
    }

    private List<ReservationWithWaitingRank> createReservationWaitingListFromReservation(
            List<ReservationWithWaitingRank> reservations) {

        List<ReservationWithWaitingRank> reservationWithWaitingRank = new ArrayList<>();

        for (int waitRank = 1; waitRank <= reservations.size(); waitRank++) {
            ReservationWithWaitingRank reservation = reservations.get(waitRank - 1);
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
