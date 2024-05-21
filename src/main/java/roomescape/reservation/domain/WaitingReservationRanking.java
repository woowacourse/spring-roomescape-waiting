package roomescape.reservation.domain;

public interface WaitingReservationRanking {

    MemberReservation getMemberReservation();

    Long getRank();

    default int getDisplayRank() {
        return getRank().intValue() + 1;
    }
}
