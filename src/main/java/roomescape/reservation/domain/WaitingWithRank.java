package roomescape.reservation.domain;

import roomescape.reservation.dto.ReservationOfMemberResponse;

import java.time.format.DateTimeFormatter;

public class WaitingWithRank {

    private Waiting waiting;
    private Rank rank;

    public WaitingWithRank(Waiting waiting, Rank rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public ReservationOfMemberResponse toReservationOfMemberResponse() {
        return new ReservationOfMemberResponse(
                waiting.getId(),
                waiting.getTheme().getName().name(),
                waiting.getDate(DateTimeFormatter.ISO_DATE),
                waiting.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                rank.getWaitingCount() + ReservationStatus.WAITING.getPrintName()
        );
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Rank getRank() {
        return rank;
    }
}
