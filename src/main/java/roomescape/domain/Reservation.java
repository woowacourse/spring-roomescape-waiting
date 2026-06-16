package roomescape.domain;

import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.domain.reservationStatus.ReservationStatus;

import java.time.LocalDate;
import java.util.Objects;

public class Reservation {

    private final Long id;
    private final String name;
    private final Long themeSlotId;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private ReservationStatus reservationStatus;
    private final String orderId;
    private final Long amount;

    public Reservation(String name, Long themeSlotId, LocalDate date, Time time, Theme theme) {
        validate(name, date, time, theme);
        this.id = null;
        this.name = name;
        this.themeSlotId = themeSlotId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = PendingStatus.getInstance();
        this.orderId = null;
        this.amount = null;
    }

    public Reservation(String name, Long themeSlotId, LocalDate date, Time time, Theme theme, String orderId, Long amount) {
        validate(name, date, time, theme);
        this.id = null;
        this.name = name;
        this.themeSlotId = themeSlotId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = PendingStatus.getInstance();
        this.orderId = orderId;
        this.amount = amount;
    }

    public Reservation(Long id, String name, Long themeSlotId, LocalDate date, Time time, Theme theme, ReservationStatus reservationStatus) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.themeSlotId = themeSlotId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = reservationStatus;
        this.orderId = null;
        this.amount = null;
    }

    public Reservation(Long id, String name, Long themeSlotId, LocalDate date, Time time, Theme theme, ReservationStatus reservationStatus, String orderId, Long amount) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.themeSlotId = themeSlotId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = reservationStatus;
        this.orderId = orderId;
        this.amount = amount;
    }

    public static Reservation of(Long id, Reservation reservation) {
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getThemeSlotId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getReservationStatus(),
                reservation.getOrderId(),
                reservation.getAmount()
        );
    }

    private void validate(String name, LocalDate date, Time time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수이며 비어있을 수 없습니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 필수입니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("유효하지 않은 예약 시간대입니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("유효하지 않은 테마입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getThemeSlotId() {
        return themeSlotId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public String getReservationStatusName() {
        return reservationStatus.getName();
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public void changeStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public void confirm() {
        reservationStatus.confirm(this);
    }

    public void cancel() {
        reservationStatus.cancel(this);
    }

    public void complete() {
        reservationStatus.complete(this);
    }

    public boolean isPendingStatus() {
        return reservationStatus == PendingStatus.getInstance();
    }

    public boolean isPending() {
        return reservationStatus == PendingStatus.getInstance();
    }

    public boolean isConfirmed() {
        return reservationStatus == ConfirmedStatus.getInstance();
    }

    public boolean isCancelled() {
        return reservationStatus == CancelledStatus.getInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
