package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationResponseDto(
        Long id,
        MemberResponseDto member,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {

    public static ReservationResponseDto from(Reservation reservationInfo) {
        ReservationTime timeInfo = reservationInfo.getReservationTime();
        Theme themeInfo = reservationInfo.getTheme();
        Member member = reservationInfo.getMember();

        return new ReservationResponseDto(
                reservationInfo.getId(),
                new MemberResponseDto(member),
                reservationInfo.getDate(),
                new ReservationTimeResponseDto(timeInfo.getId(), timeInfo.getStartAt()),
                new ThemeResponseDto(themeInfo.getId(), themeInfo.getName(), themeInfo.getDescription(),
                        themeInfo.getThumbnail())
        );
    }
}
