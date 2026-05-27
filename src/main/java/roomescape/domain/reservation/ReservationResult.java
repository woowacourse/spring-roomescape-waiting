package roomescape.domain.reservation;

import java.util.Map;

public class ReservationResult {
   private final Rank rank;
   private final Reservation reservation;

    public ReservationResult(Rank rank, Reservation reservation) {
        this.rank = rank;
        this.reservation = reservation;
    }

    public String status(){
        if (rank.isFirst()){
            return "승인";
        }

        return "대기";
    }

    public Rank getRank() {
        return rank;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
