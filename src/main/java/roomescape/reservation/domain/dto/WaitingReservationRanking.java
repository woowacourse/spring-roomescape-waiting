package roomescape.reservation.domain.dto;

import roomescape.reservation.domain.entity.MemberReservation;

public interface WaitingReservationRanking {

    MemberReservation getMemberReservation();

    Long getRank();

    default int getDisplayRank() {
        return getRank().intValue() + 1;
    }
}
