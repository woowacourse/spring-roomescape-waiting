package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Waiting {
    private final Long id;
    private final String name;
    private final Slot slot;
    private final LocalDateTime createAt;

    public static Waiting from(long id, String name, Slot slot, LocalDateTime createdAt) {
        return new Waiting(id, name, slot, createdAt);
    }

    public static Waiting create(String name, Slot slot, LocalDateTime createdAt) {

        return new Waiting(null, name, slot, createdAt);
    }

    public void validateCancelable(LocalDateTime now) {
        slot.validateAvailableTime(now);
    }

    public void validateOwnedBy(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException("타인의 예약대기는 취소할 수 없습니다.");
        }
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LocalDate waitingDate() {
        return slot.date();
    }

    public ReservationTime waitingTime() {
        return slot.time();
    }

    public Theme waitingTheme() {
        return slot.theme();
    }

    public LocalDateTime createAt() {
        return createAt;
    }
}
