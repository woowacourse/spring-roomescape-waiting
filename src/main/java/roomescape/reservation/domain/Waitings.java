package roomescape.reservation.domain;

import java.util.List;
import java.util.Optional;

public class Waitings {

    private final List<Reservation> waitings;

    public Waitings(List<Reservation> waitings) {
        this.waitings = List.copyOf(waitings);
    }

    public int findMemberRank(Reservation reservation, Long memberId) {
        if (reservation.getReservationsStatus() == ReservationStatus.SUCCESS) {
            return 0;
        }

        return (int) waitings.stream()
                .filter(waiting -> waiting.sameDate(reservation.getDate()))
                .filter(waiting -> waiting.sameThemeId(reservation.getTheme().getId()))
                .filter(waiting -> waiting.sameTimeId(reservation.getTime().getId()))
                .takeWhile(waiting -> !waiting.getMember().sameMemberId(memberId))
                .count() + 1;
    }

    public Optional<Reservation> findFirstWaitingReservationByCanceledReservation(Reservation canceledReservation) {
        return waitings.stream()
                .filter(reservation -> reservation.sameDate(canceledReservation.getDate()))
                .filter(reservation -> reservation.sameThemeId(canceledReservation.getTheme().getId()))
                .filter(reservation -> reservation.sameTimeId(canceledReservation.getTime().getId()))
                .findFirst();
    }
}
