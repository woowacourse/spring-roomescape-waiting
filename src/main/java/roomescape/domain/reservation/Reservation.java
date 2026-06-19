package roomescape.domain.reservation;

import java.time.LocalDate;
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

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status, String orderId, long quotedAmount) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.orderId = orderId;
        this.quotedAmount = quotedAmount;
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status, String orderId, long quotedAmount) {
        return new Reservation(id, name, date, time, theme, status, orderId, quotedAmount);
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, name, date, time, theme, ReservationStatus.CONFIRMED, null, 0);
    }

    public static Reservation of(String name, LocalDate date, ReservationTime time, Theme theme) {
        time.validateIfTimePast(date);
        return new Reservation(null, name, date, time, theme, ReservationStatus.CONFIRMED, null, 0);
    }

    public static Reservation pendingPayment(String name, LocalDate date, ReservationTime time, Theme theme, String orderId, long quotedAmount) {
        time.validateIfTimePast(date);
        return new Reservation(null, name, date, time, theme, ReservationStatus.PENDING_PAYMENT, orderId, quotedAmount);
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

    public ReservationSlot getSlot() {
        return ReservationSlot.of(date, time, theme);
    }
}
