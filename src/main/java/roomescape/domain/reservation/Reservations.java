package roomescape.domain.reservation;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotBookStatus;

public class Reservations {

    private static final int MAX_POPULAR_THEME_COUNT = 10;

    private final List<Reservation> reservations;

    public Reservations(final List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<TimeSlotBookStatus> checkBookStatuses(final List<TimeSlot> timeSlotsToCheck) {
        return checkBookStatus(timeSlotsToCheck, reservedTimeSlots());
    }

    private List<TimeSlot> reservedTimeSlots() {
        return reservations.stream()
            .map(Reservation::dateTime)
            .map(ReservationDateTime::timeSlot)
            .toList();
    }

    private List<TimeSlotBookStatus> checkBookStatus(final List<TimeSlot> timeSlots, final List<TimeSlot> reservedTimeSlots) {
        return timeSlots.stream()
            .map(timeSlot -> {
                var alreadyBooked = reservedTimeSlots.contains(timeSlot);
                return new TimeSlotBookStatus(timeSlot, alreadyBooked);
            })
            .toList();
    }

    public List<Theme> findPopularThemes(final int maxCount) {
        var themeReservationCounts = reservations.stream()
            .collect(groupingBy(Reservation::theme, counting()));
        var count = Math.min(maxCount, MAX_POPULAR_THEME_COUNT);
        return toPopularThemeList(count, themeReservationCounts);
    }

    private List<Theme> toPopularThemeList(final int maxCount, final Map<Theme, Long> themeReservationCounts) {
        return reservations.stream()
            .map(Reservation::theme)
            .distinct()
            .sorted(Comparator.comparing(themeReservationCounts::get).reversed())
            .limit(maxCount)
            .toList();
    }
}
