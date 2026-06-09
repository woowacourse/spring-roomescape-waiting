package roomescape.wating.domain;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.common.exception.UnprocessableContentException;
import roomescape.reservation.domain.CustomerEmail;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
public class Waiting {

    private static final String PAST_DATE_TIME_WAITING_MESSAGE = "과거 시간의 예약에 대기를 등록할 수 없습니다.";
    private static final String SLOT_REQUIRED_MESSAGE = "예약 슬롯을 입력해야 합니다.";

    private final Long id;
    private final CustomerName customerName;
    private final CustomerEmail customerEmail;
    private final LocalDateTime createdAt;
    private final ReservationSlot slot;

    private Waiting(
            Long id,
            CustomerName customerName,
            CustomerEmail customerEmail,
            LocalDateTime createdAt,
            ReservationSlot slot
    ) {
        validateRequiredValues(slot);
        this.id = id;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.createdAt = createdAt;
        this.slot = slot;
    }

    public static Waiting create(
            final String customerName,
            final String customerEmail,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final LocalDateTime now
    ) {
        return create(customerName, customerEmail, ReservationSlot.create(date, time, theme), now);
    }

    public static Waiting create(
            final String customerName,
            final String customerEmail,
            final ReservationSlot slot,
            final LocalDateTime now
    ) {
        final Waiting waiting = new Waiting(
                null,
                new CustomerName(customerName),
                new CustomerEmail(customerEmail),
                null,
                slot);

        waiting.validateNotPast(now);
        return waiting;
    }

    public static Waiting of(
            final Long id,
            final String customerName,
            final String customerEmail,
            final Date date,
            final LocalDateTime createdAt,
            final ReservationTime time,
            final Theme theme
    ) {
        return of(
                id,
                customerName,
                customerEmail,
                ReservationSlot.create(date.toLocalDate(), time, theme),
                createdAt
        );
    }

    public static Waiting of(
            final Long id,
            final String customerName,
            final String customerEmail,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        return new Waiting(
                id,
                new CustomerName(customerName),
                new CustomerEmail(customerEmail),
                createdAt,
                slot
        );
    }

    public boolean isOwnedBy(final String customerName, final String customerEmail) {
        return this.customerName.equals(new CustomerName(customerName))
                && this.customerEmail.equals(new CustomerEmail(customerEmail));
    }

    public boolean isCancelable(final LocalDateTime now) {
        return !isPastReservation(now);
    }

    public Long getSlotId() {
        return slot.getId();
    }

    public String getCustomerEmail() {
        return customerEmail.email();
    }

    public LocalDate getReservationDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    private void validateRequiredValues(final ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException(SLOT_REQUIRED_MESSAGE);
        }
    }

    private void validateNotPast(final LocalDateTime now) {
        if (isPastReservation(now)) {
            throw new UnprocessableContentException(PAST_DATE_TIME_WAITING_MESSAGE);
        }
    }

    private boolean isPastReservation(final LocalDateTime now) {
        return reservationDateTime().isBefore(now);
    }

    private LocalDateTime reservationDateTime() {
        return slot.dateTime();
    }
}
