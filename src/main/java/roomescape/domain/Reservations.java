package roomescape.domain;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class Reservations {

    private final List<Reservation> reservations;

    public Reservations(final List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<Theme> findMostBookedThemes(final int amount) {
        return reservations.stream()
                .collect(groupingBy(Reservation::getTheme, counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(amount)
                .map(Entry::getKey)
                .toList();
    }

    public Set<ReservationTime> findBookedTimes() {
        return reservations.stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
    }
}
