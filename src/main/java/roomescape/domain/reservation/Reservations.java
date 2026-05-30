package roomescape.domain.reservation;

import java.util.List;

public class Reservations {
    private final List<Reservation> reservations;

    public Reservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Rank rankOf(Reservation target) {
        long earlierCount = reservations.stream()
                .filter(r -> isEarlierThan(r, target))
                .count();
        return new Rank((int) earlierCount + 1);
    }

    private boolean isEarlierThan(Reservation source, Reservation target) {
        int byTime = source.getDateTime().compareTo(target.getDateTime());
        if (byTime != 0) {
            return byTime < 0;
        }
        return source.getId() < target.getId();
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}
