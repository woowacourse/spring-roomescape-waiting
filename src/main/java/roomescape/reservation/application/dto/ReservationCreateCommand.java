package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.timeslot.domain.TimeSlot;

public record ReservationCreateCommand(LocalDate date, long timeId, long themeId, long memberId) {

    public Reservation convertToEntity(final TimeSlot timeSlot, final Theme theme, final Member member) {
        return new Reservation(date, timeSlot, theme, member);
    }
}
