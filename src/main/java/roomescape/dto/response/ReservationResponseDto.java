package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public record ReservationResponseDto(
        Long id,
        MemberResponseDto member,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {
    public static ReservationResponseDto from(Reservation reservationInfo) {
        ReservationTime timeInfo = reservationInfo.getTime();
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
