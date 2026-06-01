package roomescape.fixture;

import java.time.LocalTime;
import roomescape.feature.time.domain.Time;

public enum TimeFixture {
    VALID_10_00(LocalTime.of(10, 0)),
    VALID_15_30(LocalTime.of(15, 30));

    private final LocalTime startAt;

    TimeFixture(LocalTime startAt) {
        this.startAt = startAt;
    }

    public Time createInstance() {
        return Time.create(startAt);
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
