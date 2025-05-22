package roomescape.domain.reservation;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
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

    public List<Waiting> checkWaitingOrders(final List<Reservation> reservationsToCheck) {
        return reservationsToCheck.stream()
            .map(this::checkOrder)
            .toList();
    }

    private Waiting checkOrder(final Reservation reservation) {
        if (!reservation.isWaiting()) {
            return new Waiting(reservation);
        }
        var queue = createQueue(reservation);
        var order = queue.orderOf(reservation);
        return new Waiting(reservation, order);
    }

    private WaitingQueue createQueue(final Reservation reservation) {
        var sameSlotReservations = new ArrayList<Reservation>();
        sameSlotReservations.add(reservation);
        sameSlotReservations.addAll(reservations.stream().filter(reservation::sameSlotWith).toList());
        return new WaitingQueue(sameSlotReservations);
    }

    public List<TimeSlotBookStatus> checkBookStatuses(final List<TimeSlot> timeSlotsToCheck) {
        return checkBookStatus(timeSlotsToCheck, reservedTimeSlots());
    }

    private List<TimeSlot> reservedTimeSlots() {
        return reservations.stream()
            .map(Reservation::slot)
            .map(ReservationSlot::timeSlot)
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
            .map(Reservation::slot)
            .collect(groupingBy(ReservationSlot::theme, counting()));
        var count = Math.min(maxCount, MAX_POPULAR_THEME_COUNT);
        return toPopularThemeList(count, themeReservationCounts);
    }

    private List<Theme> toPopularThemeList(final int maxCount, final Map<Theme, Long> themeReservationCounts) {
        return reservations.stream()
            .map(Reservation::slot)
            .map(ReservationSlot::theme)
            .distinct()
            .sorted(Comparator.comparing(themeReservationCounts::get).reversed())
            .limit(maxCount)
            .toList();
    }
}
