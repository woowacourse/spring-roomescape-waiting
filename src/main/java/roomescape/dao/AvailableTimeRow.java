package roomescape.dao;

import java.time.LocalTime;

public interface AvailableTimeRow {
    Long getId();
    LocalTime getStartAt();
    boolean isAvailable();
}
