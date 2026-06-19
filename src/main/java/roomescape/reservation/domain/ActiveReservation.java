package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.reservation.domain.exception.IllegalReservationDateTimeException;
import roomescape.reservation.domain.exception.IllegalStateReservationException;
import roomescape.reservation.domain.exception.UnauthorizedReservationChangeException;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class ActiveReservation {

    private Long id;
    private String name;
    private TimeSlot slot;
    private Long is_deleted;
    private LocalDateTime createdAt;

    public ActiveReservation withId(final Long id) {
        return ActiveReservation.builder()
                .id(id)
                .name(name)
                .slot(slot)
                .is_deleted(0L)
                .createdAt(createdAt)
                .build();
    }

    private void checkChangeable(final String username, final Clock clock) {
        if (!this.name.equals(username)) {
            throw new UnauthorizedReservationChangeException("예약 변경, 삭제 권한이 없습니다.");
        }
        if (is_deleted != 0) {
            throw new IllegalStateReservationException("이미 취소된 예약은 변경할 수 없습니다.");
        }
        if (slot.isPast(clock)) {
            throw new IllegalReservationDateTimeException("이미 지난 예약은 변경할 수 없습니다.");
        }
    }

    public ActiveReservation changeTime(final String username, final TimeSlot slot, final Clock clock) {
        checkChangeable(username, clock);
        slot.checkChangeableTime(clock);
        return ActiveReservation.builder()
                .id(id)
                .name(username)
                .slot(slot)
                .is_deleted(is_deleted)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    public ActiveReservation cancel(String username, Clock clock) {
        checkChangeable(username, clock);
        slot.checkChangeableTime(clock);
        return ActiveReservation.builder()
                .id(id)
                .name(name)
                .slot(slot)
                .is_deleted(id)
                .createdAt(createdAt)
                .build();
    }

    public PendingReservation pending(final String username, final Clock clock) {
        checkChangeable(username, clock);
        slot.checkChangeableTime(clock);
        return PendingReservation.builder()
                .id(id)
                .name(username)
                .slot(slot)
                .is_deleted(is_deleted)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    public String themeName() {
        return slot.getTheme().getName();
    }
}
