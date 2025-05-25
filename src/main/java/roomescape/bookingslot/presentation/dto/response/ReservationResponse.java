package roomescape.bookingslot.presentation.dto.response;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.response.MemberResponse;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.dto.response.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse of(BookingSlot bookingSlot, ReservationTime reservationTime, Theme theme,
                                         Member member) {
        return new ReservationResponse(bookingSlot.getId(), MemberResponse.from(member), bookingSlot.getDate(),
                ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme)
        );
    }
}
