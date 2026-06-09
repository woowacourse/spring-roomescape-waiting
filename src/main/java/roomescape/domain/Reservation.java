package roomescape.domain;

import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.ReservationStatus;

import java.time.LocalDate;
import java.util.Objects;

public class Reservation {

    private final Long id;
    private final String name;
    private final ThemeSlot themeSlot;
    private ReservationStatus reservationStatus;

    public Reservation(String name, ThemeSlot themeSlot) {
        validate(name, themeSlot);
        this.id = null;
        this.name = name;
        this.themeSlot = themeSlot;
        this.reservationStatus = PendingStatus.getInstance();
    }

    public Reservation(Long id, String name, ThemeSlot themeSlot, ReservationStatus reservationStatus) {
        validate(name, themeSlot);
        this.id = id;
        this.name = name;
        this.themeSlot = themeSlot;
        this.reservationStatus = reservationStatus;
    }

    public static Reservation of(Long id, Reservation reservation) {
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getThemeSlot(),
                reservation.getReservationStatus()
        );
    }

    private void validate(String name, ThemeSlot themeSlot) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수이며 비어있을 수 없습니다.");
        }
        if (themeSlot == null || themeSlot.getDate() == null) {
            throw new IllegalArgumentException("예약 날짜는 필수입니다.");
        }
        if (themeSlot.getTime() == null) {
            throw new IllegalArgumentException("유효하지 않은 예약 시간대입니다.");
        }
        if (themeSlot.getTheme() == null) {
            throw new IllegalArgumentException("유효하지 않은 테마입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }

    public LocalDate getDate() {
        return themeSlot.getDate();
    }

    public Time getTime() {
        return themeSlot.getTime();
    }

    public Theme getTheme() {
        return themeSlot.getTheme();
    }

    public ThemeSlot getThemeSlot() {
        return themeSlot;
    }

    public Long getThemeSlotId() {
        return themeSlot.getId();
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public String getReservationStatusName() {
        return reservationStatus.getName();
    }

    public void changeStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public void confirm() {
        reservationStatus.confirm(this);
    }

    public void waiting() {
        reservationStatus.waiting(this);
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

    public boolean isConfirmedStatus() {
        return reservationStatus == ConfirmedStatus.getInstance();
    }

    public boolean isModifiableStatus() {
        return isPendingStatus() || isConfirmedStatus();
    }

    public boolean hasDifferentThemeSlot(Long themeSlotId) {
        return !themeSlot.hasSameId(themeSlotId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
