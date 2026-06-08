package roomescape.domain.reservation;

import lombok.extern.slf4j.Slf4j;
import roomescape.domain.common.UserName;
import roomescape.domain.theme.Theme;
import roomescape.exception.ForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
public class ReservationWaiting {
    private final Long id;
    private final UserName userName;
    private final Slot slot;
    private final LocalDateTime createdAt;

    private ReservationWaiting(Long id, UserName userName, Slot slot, LocalDateTime createdAt) {
        Objects.requireNonNull(slot, "슬롯은 필수입니다.");

        this.id = id;
        this.userName = userName;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public static ReservationWaiting from(Long id, UserName name, Slot slot, LocalDateTime createdAt) {
        Objects.requireNonNull(id, "조회 및 복원시 ReservationWaiting의 id는 필수입니다.");

        return new ReservationWaiting(id, name, slot, createdAt);
    }

    public static ReservationWaiting create(UserName name, Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new ReservationWaiting(null, name, slot, now);
    }

    public void validateCancelable(LocalDateTime now) {
        slot.validateAvailableTime(now);
    }

    public void validateOwnedBy(UserName name) {
        if (!Objects.equals(userName, name)) {
            throw new ForbiddenException("타인의 예약대기는 취소할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
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

    public LocalDate getWaitingDate() {
        return slot.getDate();
    }

    public ReservationTime getWaitingTime() {
        return slot.getTime();
    }

    public Theme getWaitingTheme() {
        return slot.getTheme();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationWaiting waiting = (ReservationWaiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
