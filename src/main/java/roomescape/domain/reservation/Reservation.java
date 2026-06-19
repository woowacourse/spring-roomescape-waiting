package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

public class Reservation {

    private final Long id;
    private final String name;
    private LocalDate date;
    private ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;
    private final String orderId;
    private final long quotedAmount;
    private final LocalDateTime pendingExpiresAt;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status, String orderId, long quotedAmount, LocalDateTime pendingExpiresAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.orderId = orderId;
        this.quotedAmount = quotedAmount;
        this.pendingExpiresAt = pendingExpiresAt;
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status, String orderId, long quotedAmount, LocalDateTime pendingExpiresAt) {
        return new Reservation(id, name, date, time, theme, status, orderId, quotedAmount, pendingExpiresAt);
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, name, date, time, theme, ReservationStatus.CONFIRMED, null, 0, null);
    }

    public static Reservation of(String name, LocalDate date, ReservationTime time, Theme theme) {
        time.validateIfTimePast(date);
        return new Reservation(null, name, date, time, theme, ReservationStatus.CONFIRMED, null, 0, null);
    }

    public static Reservation pendingPayment(String name, LocalDate date, ReservationTime time, Theme theme, String orderId, long quotedAmount, LocalDateTime pendingExpiresAt) {
        time.validateIfTimePast(date);
        return new Reservation(null, name, date, time, theme, ReservationStatus.PENDING_PAYMENT, orderId, quotedAmount, pendingExpiresAt);
    }

    public void validateOwner(String newRequestOwner) {
        if (!name.equals(newRequestOwner)) {
            throw new RoomescapeException(ErrorCode.UNAUTHORIZED_NAME);
        }
    }

    public boolean isOwner(String name) {
        return this.name.equals(name);
    }

    public void changeSchedule(LocalDate newDate, ReservationTime newTime) {
        newTime.validateIfTimePast(newDate);
        this.date = newDate;
        this.time = newTime;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public ReservationTime getTime() { return time; }
    public Theme getTheme() { return theme; }
    public ReservationStatus getStatus() { return status; }
    public String getOrderId() { return orderId; }
    public long getQuotedAmount() { return quotedAmount; }
    public LocalDateTime getPendingExpiresAt() { return pendingExpiresAt; }

    public ReservationSlot getSlot() {
        return ReservationSlot.of(date, time, theme);
    }
}
