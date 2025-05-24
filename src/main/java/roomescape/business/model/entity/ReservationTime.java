package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

import java.time.LocalTime;

@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Entity
public class ReservationTime {

    private static final int MINUTE_INTERVAL = 30;

    @EmbeddedId
    private final Id id = Id.issue();
    @Embedded
    private final StartTime startTime;

    public ReservationTime(final LocalTime startTime) {
        this.startTime = new StartTime(startTime);
    }

    public LocalTime startInterval() {
        return startTime.minusMinutes(MINUTE_INTERVAL);
    }

    public LocalTime endInterval() {
        return startTime.plusMinutes(MINUTE_INTERVAL);
    }

    public LocalTime startTimeValue() {
        return startTime.value();
    }
}
