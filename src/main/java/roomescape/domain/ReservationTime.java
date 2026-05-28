package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private TimeStatus status;

    public ReservationTime(Long id, LocalTime startAt, TimeStatus status) {
        if (startAt == null) {
            throw new RoomEscapeException("추가 할 예약 시작 시간 정보가 누락되었습니다.");
        }
        this.id = id;
        this.startAt = startAt;
        this.status = status;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt, TimeStatus.ACTIVE);
    }

    public boolean isPast(LocalDate date) {
        return LocalDateTime.of(date, this.startAt).isBefore(LocalDateTime.now());
    }

    public void deactivate() {
        if (this.status == TimeStatus.INACTIVE) {
            throw new RoomEscapeException("이미 비활성화 된 시간 정보입니다.");
        }
        this.status = TimeStatus.INACTIVE;
    }

    public void activate() {
        if (this.status == TimeStatus.ACTIVE) {
            throw new RoomEscapeException("이미 활성화 된 시간 정보입니다.");
        }
        this.status = TimeStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == TimeStatus.ACTIVE;
    }
}
