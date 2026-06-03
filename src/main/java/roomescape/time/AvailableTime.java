package roomescape.time;

import java.time.LocalTime;

public class AvailableTime {
    private final LocalTime time;
    private final boolean isAvailable;

    public AvailableTime(LocalTime time, boolean isAvailable) {
        this.time = time;
        this.isAvailable = isAvailable;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }
}
