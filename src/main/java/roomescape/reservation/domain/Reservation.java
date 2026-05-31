package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Reservation {

    private final Long id;
    private final CustomerName customerName;
    private final ReservationSlot slot;

    private Reservation(final Long id, final CustomerName customerName, final ReservationSlot slot) {
        validateRequiredValues(slot);

        this.id = id;
        this.customerName = customerName;
        this.slot = slot;
    }

    public static Reservation create(
            final String name,
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final LocalDateTime now
    ) {
        return create(name, ReservationSlot.create(date, reservationTime, theme), now);
    }

    public static Reservation create(
            final String name,
            final ReservationSlot slot,
            final LocalDateTime now
    ) {
        final Reservation reservation = new Reservation(
                null,
                new CustomerName(name),
                slot
        );

        reservation.validateNotPast(now);
        return reservation;
    }

    public static Reservation of(
            final Long id,
            final String name,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme) {
        return of(id, name, ReservationSlot.create(date, time, theme));
    }

    public static Reservation of(
            final Long id,
            final String name,
            final ReservationSlot slot
    ) {
        return new Reservation(
                id,
                new CustomerName(name),
                slot
        );
    }

    public Reservation changeSchedule(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime now
    ) {
        return changeSchedule(ReservationSlot.create(date, time, slot.getTheme()), now);
    }

    public Reservation changeSchedule(
            final ReservationSlot slot,
            final LocalDateTime now
    ) {
        final Reservation changed = new Reservation(
                id,
                customerName,
                slot
        );

        changed.validateNotPast(now);
        return changed;
    }

    public String getCustomerName() {
        return customerName.name();
    }

    public Long getSlotId() {
        return slot.getId();
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public void validateCancelableByCustomer(final LocalDate today) {
        if (!isBeforeReservationDate(today)) {
            throw new ReservationCancellationException();
        }
    }

    public void validateModifiableByCustomer(final LocalDate today) {
        if (!isBeforeReservationDate(today)) {
            throw new ReservationModificationException();
        }
    }

    private boolean isBeforeReservationDate(final LocalDate today) {
        return today.isBefore(slot.getDate());
    }

    private void validateRequiredValues(final ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯을 입력해야 합니다.");
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
        return slot.dateTime();
    }
}
