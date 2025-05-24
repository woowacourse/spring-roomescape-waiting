package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;

@Embeddable
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReservationDate {

    private static final Map<LocalDate, ReservationDate> CACHE = new HashMap<>();

    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private final LocalDate date;

    protected ReservationDate() {
        this(null);
    }

    public boolean isEqualToDate(final LocalDate date) {
        return this.date.isEqual(date);
    }

    public static ReservationDate of(final LocalDate date, final LocalDate today) {
        validatePastDate(date, today);
        return CACHE.computeIfAbsent(date, ReservationDate::new);
    }

    public static ReservationDate fromQuery(final LocalDate date){
        return CACHE.computeIfAbsent(date, ReservationDate::new);
    }

    private static void validatePastDate(final LocalDate date, final LocalDate today) {
        if (date.isBefore(today)) {
            throw new ReservationPastDateException();
        }
    }

    public LocalDate date() {
        return date;
    }

}
