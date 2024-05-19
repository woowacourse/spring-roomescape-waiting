package roomescape.dto.reservation;

import roomescape.domain.member.Member;
import roomescape.domain.member.Name;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.MemberResponse;
import roomescape.dto.theme.ThemeResponse;

public record AdminReservationSaveRequest (
        Long memberId,
        String date,
        Long timeId,
        Long themeId
) {

    public Reservation toModel(
            final MemberResponse memberResponse,
            final ThemeResponse themeResponse,
            final ReservationTimeResponse timeResponse
    ) {
        final Member member = new Member(memberResponse.id(), new Name(memberResponse.name()), memberResponse.email());
        final ReservationTime time = new ReservationTime(timeResponse.id(), timeResponse.startAt());
        final Theme theme = new Theme(themeResponse.id(), themeResponse.name(), themeResponse.description(), themeResponse.thumbnail());
        return new Reservation(member, date, time, theme);
    }
}
