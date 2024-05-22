package roomescape.reservation.domain;

import java.util.List;

public class Waitings {

    private final List<Reservation> waitings;

    public Waitings(List<Reservation> waitings) {
        this.waitings = List.copyOf(waitings);
    }

    public int findMemberRank(Reservation reservation, Long memberId) {
        if (reservation.getStatus() == Status.SUCCESS) {
            return 0;
        }

        return (int) waitings.stream()
                .filter(waiting -> waiting.getTheme().sameThemeId(reservation.getTheme().getId()))
                .filter(waiting -> waiting.sameDate(reservation.getDate()))
                .filter(waiting -> waiting.getTime().sameTimeId(reservation.getTime().getId()))
                .takeWhile(waiting -> !waiting.getMember().sameMemberId(memberId))
                .count() + 1;
    }
}
