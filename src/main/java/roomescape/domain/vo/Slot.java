package roomescape.domain.vo;

import java.time.LocalDate;

import roomescape.domain.Theme;
import roomescape.domain.entity.Time;

public record Slot (long slotId, LocalDate date, Time startAt, Theme theme){
}
