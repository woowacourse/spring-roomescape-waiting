package roomescape.domain.reservation;

import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;

public class ReservationWithRank extends Reservation {

    private final int rank;

    public ReservationWithRank(Long id, Member Member, String rawDate, ReservationTime time, Theme theme, ReservationStatus status, int rank) {
        super(id, Member, rawDate, time, theme, status);
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
