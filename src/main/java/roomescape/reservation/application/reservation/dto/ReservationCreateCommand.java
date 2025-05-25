package roomescape.reservation.application.reservation.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.timeslot.TimeSlot;

public record ReservationCreateCommand(LocalDate date, long memberId, long timeId, long themeId) {

    public Reservation convertToEntity(final Member member, final TimeSlot timeSlot, final Theme theme) {
        return new Reservation(date, member, timeSlot, theme);
    }
}
