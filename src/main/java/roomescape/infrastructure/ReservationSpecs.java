package roomescape.infrastructure;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.RoomescapeSchedule;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationSpecs {

    public static Specification<Reservation> byDate(final LocalDate date) {
        return (reservation, query, cb) -> cb.equal(dateOf(reservation), date);
    }

    public static Specification<Reservation> byDateBetween(final LocalDate from, final LocalDate to) {
        return (reservation, query, cb) -> cb.between(dateOf(reservation), from, to);
    }

    public static Specification<Reservation> byTimeSlotId(final long id) {
        return (reservation, query, cb) -> cb.equal(timeSlotIdOf(reservation), id);
    }

    public static Specification<Reservation> byThemeId(final long id) {
        return (reservation, query, cb) -> cb.equal(themeIdOf(reservation), id);
    }

    public static Specification<Reservation> byStatus(final ReservationStatus status) {
        return (reservation, query, cb) -> cb.equal(statusOf(reservation), status);
    }

    public static Specification<Reservation> bySchedule(final RoomescapeSchedule schedule) {
        return allOf(byDate(schedule.date()), byTimeSlotId(schedule.timeSlot().id()), byThemeId(schedule.theme().id()));
    }

    public static Specification<Reservation> byFilter(final ReservationSearchFilter filter) {
        return (reservation, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.themeId() != null) {
                predicates.add(cb.equal(themeIdOf(reservation), filter.themeId()));
            }
            if (filter.userId() != null) {
                predicates.add(cb.equal(userIdOf(reservation), filter.userId()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(dateOf(reservation), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(dateOf(reservation), filter.dateTo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SafeVarargs
    public static Specification<Reservation> allOf(final Specification<Reservation>... specifications) {
        return Specification.allOf(specifications);
    }

    private static Path<RoomescapeSchedule> roomescapeScheduleOf(final Root<Reservation> reservation) {
        return reservation.get("reservedSchedule");
    }

    private static Path<LocalDate> dateOf(final Root<Reservation> reservation) {
        return roomescapeScheduleOf(reservation).get("dateTime").get("date");
    }

    private static Path<Long> timeSlotIdOf(final Root<Reservation> reservation) {
        return roomescapeScheduleOf(reservation).get("dateTime").get("timeSlot").get("id");
    }

    private static Path<Long> themeIdOf(final Root<Reservation> reservation) {
        return roomescapeScheduleOf(reservation).get("theme").get("id");
    }

    private static Path<ReservationStatus> statusOf(final Root<Reservation> reservation) {
        return reservation.get("status");
    }

    private static Path<Long> userIdOf(final Root<Reservation> reservation) {
        return reservation.get("user").get("id");
    }
}
