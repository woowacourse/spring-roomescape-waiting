package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;

public class Waitings {
    private final List<Reservation> waitings;

    public Waitings(List<Reservation> waitingReservations) {
        waitings = getWaitings(waitingReservations);
    }

    private List<Reservation> getWaitings(List<Reservation> waitingReservations) {
        if (!waitingReservations.isEmpty()) {
            return waitingReservations.stream()
                    .sorted(Comparator.comparing(Reservation::getCreatedAt))
                    .toList();
        }
        return waitingReservations;
    }

    public int findMemberRank(Reservation reservation, Long memberId) {
        if (reservation.getStatus() == Status.SUCCESS) {
            return 0;
        }

        return (int) waitings.stream()
                .filter(waiting -> waiting.getTheme().sameThemeId(reservation.getTheme().getId()))
                .filter(waiting -> waiting.getDate().equals(reservation.getDate()))
                .filter(waiting -> waiting.getTime().getStartAt().equals(reservation.getTime().getStartAt()))
                .takeWhile(waiting -> !waiting.getMember().sameMemberId(memberId))
                .count() + 1;
    }

    public Reservation getFirstWaiting() {
        return waitings.get(0);
    }

    public boolean haveWaiting() {
        return !waitings.isEmpty();
    }
}
