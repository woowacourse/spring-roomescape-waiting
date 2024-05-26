package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public record ReservationAddRequest(LocalDate date, Time time, Theme theme, Member member) {
}
