package roomescape.service.reserveticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import roomescape.domain.member.Reserver;
import roomescape.domain.reserveticket.ReservationWithWaitingRanks;
import roomescape.domain.reserveticket.ReserveTicketFinder;

public class ReserveTicketWaitings {

    private final ReserveTicketFinder memberReservations;
    private final ReservationWithWaitingRanks reservationWithWaitingRanks;
    private final List<ReserveTicketWaiting> reserveTicketWaitings;

    public ReserveTicketWaitings(ReserveTicketFinder memberReservations,
                                 ReservationWithWaitingRanks reservationWithWaitingRanks) {
        this.memberReservations = memberReservations;
        this.reservationWithWaitingRanks = reservationWithWaitingRanks;
        this.reserveTicketWaitings = calculateReserveTicketWaitings();
    }

    private List<ReserveTicketWaiting> calculateReserveTicketWaitings() {
        List<ReserveTicketWaiting> pendingReserveTicketWaitings = new ArrayList<>();
        List<ReservationWithWaitingRank> reservationWithRanks = reservationWithWaitingRanks.getReservationWithRanks();
        for (ReservationWithWaitingRank reservationWithWaitingRank : reservationWithRanks) {
            Reserver reserver = memberReservations.findReserverFromReservation(reservationWithWaitingRank.getId());
            pendingReserveTicketWaitings.add(new ReserveTicketWaiting(reservationWithWaitingRank, reserver));
        }
        return pendingReserveTicketWaitings;
    }

    public List<ReserveTicketWaiting> reserveTicketWaitings() {
        return Collections.unmodifiableList(reserveTicketWaitings);
    }
}
