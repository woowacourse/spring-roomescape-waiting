package roomescape.domain.reserveticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import roomescape.domain.reservation.Reservation;
import roomescape.service.reserveticket.ReserveTicketWaiting;

public class ReservationTicketWaitings {

    private final Map<Reservation, List<ReserveTicket>> reserveTicketMap = new HashMap<>();

    public ReservationTicketWaitings(List<ReserveTicket> reserveTickets) {
        Set<Reservation> reservations = new HashSet<>();
        for (ReserveTicket reserveTicket : reserveTickets) {
            reserveTicketMap.putIfAbsent(reserveTicket.getReservation(), new ArrayList<>());
            List<ReserveTicket> reserveTicketList = reserveTicketMap.getOrDefault(reserveTicket.getReservation(),
                    new ArrayList<>());
            reserveTicketList.add(reserveTicket);
            reservations.add(reserveTicket.getReservation());
            reserveTicketMap.put(reserveTicket.getReservation(), reserveTicketList);
        }
    }

    public List<ReserveTicketWaiting> reserveTicketWaitings() {
        List<ReserveTicketWaiting> reservationTicketWaitings = new ArrayList<>();
        List<Reservation> currentReservations = new ArrayList<>(reserveTicketMap.keySet().stream().toList());
        Collections.sort(currentReservations, (o1, o2) -> (int) (o1.getId() - o2.getId()));

        for (Reservation reservation : currentReservations) {
            List<ReserveTicketWaiting> reservationToReserveTicketWaitings = createReserveTicketWaitingFromReservation(
                    reservation);
            reservationTicketWaitings.addAll(reservationToReserveTicketWaitings);
        }
        return Collections.unmodifiableList(reservationTicketWaitings);
    }

    private List<ReserveTicketWaiting> createReserveTicketWaitingFromReservation(Reservation reservation) {
        List<ReserveTicket> reserveTickets = reserveTicketMap.get(reservation);
        List<ReserveTicketWaiting> reserveTicketWaitings = new ArrayList<>();
        for (int ticketRank = 1; ticketRank <= reserveTickets.size(); ticketRank++) {
            ReserveTicket reserveTicket = reserveTickets.get(ticketRank - 1);
            reserveTicketWaitings.add(
                    new ReserveTicketWaiting(reserveTicket.getId(), reserveTicket.getName(), reservation.getDate(),
                            reservation.getStartAt(), reservation.getReservationStatus(), ticketRank,
                            reservation.getThemeName(), reserveTicket.getMemberId()));
        }
        return reserveTicketWaitings;
    }
}
