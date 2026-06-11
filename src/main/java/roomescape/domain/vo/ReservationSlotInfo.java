package roomescape.domain.vo;

import java.time.LocalDate;

import roomescape.domain.Theme;
import roomescape.domain.Time;

public record ReservationSlotInfo(Long slotId, LocalDate date, Time time, Theme theme){
}
