package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.DomainPreconditions;
import roomescape.domain.exception.RoomEscapeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;
    private final String orderId;
    private final String idempotencyKey;
    private final Long amount;
    private final String paymentKey;

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        this(id, name, date, time, theme, ReservationStatus.CONFIRMED, null, null, null, null);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
                       ReservationStatus status, String orderId, Long amount, String paymentKey) {
        this(id, name, date, time, theme, status, orderId, orderId, amount, paymentKey);
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
                       ReservationStatus status, String orderId, String idempotencyKey, Long amount, String paymentKey) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = DomainPreconditions.requireNonNull(status, DomainErrorCode.INVALID_INPUT, "status");
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public static Reservation of(Long id, Reservation reservation) {
        return new Reservation(id, reservation.name, reservation.date, reservation.time, reservation.theme,
                reservation.status, reservation.orderId, reservation.idempotencyKey, reservation.amount,
                reservation.paymentKey);
    }

    public static Reservation pending(String name, LocalDate date, ReservationTime time, Theme theme,
                                      String orderId, Long amount) {
        return pending(name, date, time, theme, orderId, orderId, amount);
    }

    public static Reservation pending(String name, LocalDate date, ReservationTime time, Theme theme,
                                      String orderId, String idempotencyKey, Long amount) {
        validateOrderId(orderId);
        DomainPreconditions.requireNonBlank(idempotencyKey, DomainErrorCode.INVALID_INPUT, "idempotencyKey");
        DomainPreconditions.require(idempotencyKey.length() >= 6 && idempotencyKey.length() <= 64,
                DomainErrorCode.INVALID_INPUT, "idempotencyKey");
        DomainPreconditions.require(idempotencyKey.matches("[A-Za-z0-9_-]+"), DomainErrorCode.INVALID_INPUT,
                "idempotencyKey");
        DomainPreconditions.requireNonNull(amount, DomainErrorCode.INVALID_INPUT, "amount");
        return new Reservation(null, name, date, time, theme, ReservationStatus.PENDING, orderId, idempotencyKey,
                amount, null);
    }

    public Reservation confirmPayment(String paymentKey) {
        DomainPreconditions.requireNonBlank(paymentKey, DomainErrorCode.INVALID_INPUT, "paymentKey");
        return new Reservation(id, name, date, time, theme, ReservationStatus.CONFIRMED, orderId, idempotencyKey,
                amount, paymentKey);
    }

    public Reservation markPaymentUnknown() {
        return new Reservation(id, name, date, time, theme, ReservationStatus.PAYMENT_UNKNOWN, orderId, idempotencyKey,
                amount, paymentKey);
    }

    public void validateCreatable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_CREATE);
        }
    }

    public void validateDeletable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(DomainErrorCode.PAST_RESERVATION_DELETE);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }

    public boolean needsPaymentConfirmation() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.PAYMENT_UNKNOWN;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    public boolean isReservedBy(String name) {
        return this.name.equals(name);
    }

    private boolean isPast(LocalDateTime now) {
        if (date.isBefore(now.toLocalDate())) {
            return true;
        }
        if (date.isAfter(now.toLocalDate())) {
            return false;
        }
        return time.isPast(now.toLocalTime());
    }

    private void validate(String name, LocalDate date, ReservationTime time, Theme theme) {
        DomainPreconditions.requireNonBlank(name, DomainErrorCode.INVALID_INPUT, "name");
        DomainPreconditions.requireNonNull(date, DomainErrorCode.INVALID_INPUT, "date");
        DomainPreconditions.requireNonNull(time, DomainErrorCode.INVALID_INPUT, "time");
        DomainPreconditions.requireNonNull(theme, DomainErrorCode.INVALID_INPUT, "theme");
    }

    private static void validateOrderId(String orderId) {
        DomainPreconditions.requireNonBlank(orderId, DomainErrorCode.INVALID_INPUT, "orderId");
        DomainPreconditions.require(orderId.length() >= 6 && orderId.length() <= 64,
                DomainErrorCode.INVALID_INPUT, "orderId");
        DomainPreconditions.require(orderId.matches("[A-Za-z0-9_-]+"), DomainErrorCode.INVALID_INPUT, "orderId");
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name)
                && Objects.equals(date, that.date) && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme) && status == that.status
                && Objects.equals(orderId, that.orderId) && Objects.equals(idempotencyKey, that.idempotencyKey)
                && Objects.equals(amount, that.amount) && Objects.equals(paymentKey, that.paymentKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, time, theme, status, orderId, idempotencyKey, amount, paymentKey);
    }
}
