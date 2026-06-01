package roomescape.reservationtime.domain;

import java.time.LocalTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "startAt")
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    @Builder
    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = requireStartAt(startAt);
    }

    public ReservationTime withId(Long generatedId) {
        return ReservationTime.builder()
                .id(generatedId)
                .startAt(this.startAt)
                .build();
    }

    private static LocalTime requireStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new RoomEscapeException("시간은 비어있을 수 없습니다.");
        }
        return startAt;
    }
}
