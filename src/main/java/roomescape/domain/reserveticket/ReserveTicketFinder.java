package roomescape.domain.reserveticket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import roomescape.domain.member.Reserver;

public class ReserveTicketFinder {

    private final Map<Long, Reserver> reservationMemberMap;

    public ReserveTicketFinder(List<ReserveTicket> reserveTickets) {
        Map<Long, Reserver> reservationReserverMap = new HashMap<>();
        for (ReserveTicket reserveTicket : reserveTickets) {
            reservationReserverMap.put(reserveTicket.getReservation().getId(), reserveTicket.getMember());
        }

        this.reservationMemberMap = reservationReserverMap;
    }

    public Reserver findReserverFromReservation(Long id) {
        return reservationMemberMap.get(id);
    }
}
