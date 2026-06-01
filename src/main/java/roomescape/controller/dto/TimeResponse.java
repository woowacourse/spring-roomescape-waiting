package roomescape.controller.dto;

import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;

import java.time.LocalTime;

public record TimeResponse(long id, LocalTime startAt, boolean isAvailable) {

    public static TimeResponse from(Time time) {
        return new TimeResponse(time.getId(), time.getStartAt(), true);
    }

    public static TimeResponse from(ThemeSlot themeSlot) {
        return new TimeResponse(themeSlot.getId(), themeSlot.getTime().getStartAt(), !themeSlot.isReserved());
    }

}
