package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.timeslot.domain.TimeSlot;

public record ReservationCreateCommand(LocalDate date, long memberId, long timeId, long themeId) {

    public Reservation convertToEntity(final Member member, final TimeSlot timeSlot, final Theme theme) {
        return new Reservation(date, member, timeSlot, theme);
    }
}
