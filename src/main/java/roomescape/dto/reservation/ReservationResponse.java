package roomescape.dto.reservation;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member
) {

    public static ReservationResponse from(Reservation reservation) {
        ReservationTimeResponse reservationTimeResponse = getReservationTimeResponse(reservation.getTime());
        ThemeResponse themeResponse = getThemeResponse(reservation.getTheme());
        MemberResponse memberResponse = getMemberResponse(reservation.getMember());

        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservationTimeResponse,
                themeResponse,
                memberResponse
        );
    }

    private static ReservationTimeResponse getReservationTimeResponse(ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }

    private static ThemeResponse getThemeResponse(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getThemeName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    private static MemberResponse getMemberResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
