package roomescape.domain.reservation;

import java.util.List;

public class RankedReservation {
    private final Rank rank;
    private final Reservation reservation;

    private RankedReservation(Rank rank, Reservation reservation) {
        this.rank = rank;
        this.reservation = reservation;
    }

    public static RankedReservation decideRankFrom(Reservation target, List<Reservation> reservations) {
        long earlierCount = reservations.stream()
                .filter(r -> r.isEarlierThan(target))
                .count();
        return new RankedReservation(new Rank((int) earlierCount), target);
    }

    public Rank getRank() {
        return rank;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
