package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Reservation {

    private final Long id;
    private final CustomerName customerName;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(final Long id, final CustomerName customerName, final LocalDate date, final ReservationTime time, final Theme theme) {
        validateRequiredValues(date, time);

        this.id = id;
        this.customerName = customerName;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation create(
            final String name,
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final LocalDateTime now
    ) {
        final Reservation reservation = new Reservation(null, new CustomerName(name), date, reservationTime, theme);

        reservation.validateNotPast(now);
        return reservation;
    }

    public static Reservation of(
            final Long id,
            final String name,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme) {
        return new Reservation(id, new CustomerName(name), date, time, theme);
    }

    public static Reservation promote(
            final CustomerName customerName,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        return new Reservation(null, customerName, date, time, theme);
    }

    public Reservation changeSchedule(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime now
    ) {
        final Reservation changed = new Reservation(id, customerName, date, time, theme);

        changed.validateNotPast(now);
        return changed;
    }

    public String getCustomerName() {
        return customerName.name();
    }

    public void validateCancelableByCustomer(final LocalDate today) {
        if (isReservationDatePastOrToday(today)) {
            throw new ReservationCancellationException();
        }
    }

    public void validateModifiableByCustomer(final LocalDate today) {
        if (isReservationDatePastOrToday(today)) {
            throw new ReservationModificationException();
        }
    }

    public boolean isFutureSlot(final LocalDateTime now) {
        return reservationDateTime().isAfter(now);
    }

    private boolean isReservationDatePastOrToday(final LocalDate today) {
        return !today.isBefore(date);
    }

    private void validateRequiredValues(final LocalDate date, final ReservationTime time) {
        if (date == null) {
            throw new IllegalArgumentException("예약일을 입력해야 합니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("예약 시간을 입력해야 합니다.");
        }
    }

    private void validateNotPast(final LocalDateTime now) {
        if (isPast(now)) {
            throw new IllegalArgumentException("과거 시간으로는 예약할 수 없습니다.");
        }
    }

    private boolean isPast(final LocalDateTime now) {
        return reservationDateTime().isBefore(now);
    }

    private LocalDateTime reservationDateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }
}
