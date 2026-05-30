package roomescape.domain.waiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.ForbiddenException;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;

public class Waiting {
    private final Long id;
    private final UserName userName;
    private final Slot slot;
    private final LocalDateTime createdAt;

    public Waiting(UserName userName, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        this(null, userName, date, time, theme, createdAt);
    }

    public Waiting(UserName userName, Slot slot, LocalDateTime createdAt) {
        this(null, userName, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    public Waiting(Long id, UserName userName, Slot slot, LocalDateTime createdAt) {
        this(id, userName, slot.date(), slot.time(), slot.theme(), createdAt);
    }

    public Waiting(Long id, UserName userName, LocalDate date, ReservationTime time, Theme theme,
                   LocalDateTime createdAt) {
        this.id = id;
        validate(userName, date, time, theme, createdAt);
        this.userName = userName;
        this.slot = Slot.from(date, time, theme);
        this.createdAt = createdAt;
    }

    private void validate(UserName userName, LocalDate date, ReservationTime time, Theme theme,
                          LocalDateTime createdAt) {
        Objects.requireNonNull(userName, "예약자 이름이 비어 있습니다.");
        Objects.requireNonNull(date, "예약 날짜가 비어 있습니다.");
        Objects.requireNonNull(time, "시간이 비어 있습니다.");
        Objects.requireNonNull(theme, "테마가 비어 있습니다.");
        Objects.requireNonNull(createdAt, "대기 신청 시간이 비어 있습니다.");
    }

    public void cancel(UserName userName) {
        validateOwner(userName, "다른 사람의 예약 대기는 취소할 수 없습니다.");
    }

    private void validateOwner(UserName userName, String message) {
        if (!userName.equals(this.userName)) {
            throw new ForbiddenException(message);
        }
    }

    public Long getId() {
        return id;
    }

    public UserName getName() {
        return userName;
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
