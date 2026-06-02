package roomescape.reservationtime.domain;

import java.time.LocalTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(of = "startAt")
public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTime withId(Long generatedId) {
        return ReservationTime.builder()
                .id(generatedId)
                .startAt(this.startAt)
                .build();
    }
}
