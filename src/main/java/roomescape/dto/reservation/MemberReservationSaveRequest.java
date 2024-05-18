package roomescape.dto.reservation;

import roomescape.dto.MemberResponse;

public record MemberReservationSaveRequest(
        String date,
        Long timeId,
        Long themeId
) {

    public ReservationSaveRequest generateReservationSaveRequest(MemberResponse memberResponse) {
        return new ReservationSaveRequest(memberResponse.id(), date, timeId, themeId);
    }
}
