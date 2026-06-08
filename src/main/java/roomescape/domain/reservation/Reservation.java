package roomescape.domain.reservation;

import roomescape.domain.common.UserName;
import roomescape.domain.theme.Theme;
import roomescape.exception.ForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private final Long id;
    private final UserName userName;
    private final Slot slot;

    private Reservation(Long id, UserName username, Slot slot) {
        validateFields(username, slot);

        this.id = id;
        this.userName = username;
        this.slot = slot;
    }

    private void validateFields(UserName username, Slot slot) {
        Objects.requireNonNull(username, "예약자 이름은 필수입니다.");
        Objects.requireNonNull(slot, "예약 슬롯은 필수입니다.");
    }

    public static Reservation from(Long id, UserName username, Slot slot) {
        Objects.requireNonNull(id, "조회 및 복원시 Reservation의 id는 필수입니다.");
        return new Reservation(id, username, slot);
    }

    public static Reservation create(UserName username, Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Reservation(null, username, slot);
    }

    public static Reservation promote(UserName userName, Slot slot) {
        return new Reservation(null, userName, slot);
    }

    public boolean isOwnedBy(UserName name) {
        return Objects.equals(userName, name);
    }

    public void validateOwnedBy(UserName name) {
        if (!this.isOwnedBy(name)) {
            throw new ForbiddenException("타인의 예약에 접근할 수 없습니다.");
        }
    }

    public Reservation withSlot(Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Reservation(this.id, this.userName, slot);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public void validateCancelable(LocalDateTime now) {
        slot.validateAvailableTime(now);
    }

    public UserName getUserName() {
        return userName;
    }

    public Slot getSlot() {
        return slot;
    }

    public String getUserNameValue() {
        return userName.getName();
    }

    public LocalDate getReservationDate() {
        return slot.getDate();
    }

    public ReservationTime getReservationTime() {
        return slot.getTime();
    }

    public Theme getReservationTheme() {
        return slot.getTheme();
    }

    public Long getId() {
        return id;
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
        return Objects.hashCode(id);
    }
}
