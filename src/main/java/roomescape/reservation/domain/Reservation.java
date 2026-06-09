package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.common.exception.ConflictException;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Reservation {

    private static final String RESERVATION_CANCELLATION_MESSAGE = "당일 예약은 취소할 수 없습니다.";
    private static final String SLOT_REQUIRED_MESSAGE = "예약 슬롯을 입력해야 합니다.";
    private static final String PAST_RESERVATION_MESSAGE = "과거 시간으로는 예약할 수 없습니다.";

    private final Long id;
    private final CustomerName customerName;
    private final CustomerEmail customerEmail;
    private final ReservationSlot slot;

    private Reservation(
            final Long id,
            final CustomerName customerName,
            final CustomerEmail customerEmail,
            final ReservationSlot slot
    ) {
        validateRequiredValues(slot);

        this.id = id;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.slot = slot;
    }

    public static Reservation create(
            final String name,
            final String email,
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final LocalDateTime now
    ) {
        return create(name, email, ReservationSlot.create(date, reservationTime, theme), now);
    }

    public static Reservation create(
            final String name,
            final String email,
            final ReservationSlot slot,
            final LocalDateTime now
    ) {
        final Reservation reservation = new Reservation(
                null,
                new CustomerName(name),
                new CustomerEmail(email),
                slot
        );

        reservation.validateNotPast(now);
        return reservation;
    }

    public static Reservation of(
            final Long id,
            final String name,
            final String email,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme) {
        return of(id, name, email, ReservationSlot.create(date, time, theme));
    }

    public static Reservation of(
            final Long id,
            final String name,
            final String email,
            final ReservationSlot slot
    ) {
        return new Reservation(
                id,
                new CustomerName(name),
                new CustomerEmail(email),
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
                customerEmail,
                slot
        );

        changed.validateNotPast(now);
        return changed;
    }

    public String getCustomerName() {
        return customerName.name();
    }

    public String getCustomerEmail() {
        return customerEmail.email();
    }

    public boolean isOwnedBy(final String customerName, final String customerEmail) {
        return this.customerName.equals(new CustomerName(customerName))
                && this.customerEmail.equals(new CustomerEmail(customerEmail));
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
            throw new ConflictException(RESERVATION_CANCELLATION_MESSAGE);
        }
    }

    private boolean isBeforeReservationDate(final LocalDate today) {
        return today.isBefore(slot.getDate());
    }

    private void validateRequiredValues(final ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException(SLOT_REQUIRED_MESSAGE);
        }
    }

    private void validateNotPast(final LocalDateTime now) {
        if (isPast(now)) {
            throw new IllegalArgumentException(PAST_RESERVATION_MESSAGE);
        }
    }

    private boolean isPast(final LocalDateTime now) {
        return reservationDateTime().isBefore(now);
    }

    private LocalDateTime reservationDateTime() {
        return slot.dateTime();
    }
}
