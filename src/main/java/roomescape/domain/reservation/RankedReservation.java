package roomescape.domain.reservation;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RankedReservation {
    private final Rank rank;
    private final Reservation reservation;

    public static RankedReservation decideRankFrom(Reservation target, List<Reservation> reservations) {
        long earlierCount = reservations.stream()
                .filter(r -> r.isEarlierThan(target))
                .count();
        return new RankedReservation(new Rank((int) earlierCount), target);
    }
}
