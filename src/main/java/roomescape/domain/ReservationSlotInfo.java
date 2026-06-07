package roomescape.domain;

import java.time.LocalDate;

public record ReservationSlotInfo(Long slotId, LocalDate date, Time time, Theme theme){
}
