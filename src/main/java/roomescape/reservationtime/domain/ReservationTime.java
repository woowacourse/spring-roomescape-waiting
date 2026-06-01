package roomescape.reservationtime.domain;

import java.time.LocalTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;
import roomescape.global.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "startAt")
public class ReservationTime {

    @With
    private final Long id;
    private final LocalTime startAt;

    @Builder
    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = requireStartAt(startAt);
    }

    private static LocalTime requireStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new RoomEscapeException("시간은 비어있을 수 없습니다.");
        }
        return startAt;
    }
}
