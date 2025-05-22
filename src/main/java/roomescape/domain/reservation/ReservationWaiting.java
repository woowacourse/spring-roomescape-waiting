package roomescape.domain.reservation;

import java.time.LocalDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;

public class ReservationWaiting {
    private Reservation reservation;
    private int rank;

    public ReservationWaiting(LocalDate date, ReservationTime time, Theme theme, Member member, int rank) {
        this.reservation = Reservation.createWaitingWithoutId(
                member, date, time, theme
        );
        this.rank = rank;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getRank() {
        return rank;
    }
}
