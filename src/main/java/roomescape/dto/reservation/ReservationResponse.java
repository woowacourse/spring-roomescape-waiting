package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.member.MemberNameResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.dto.time.ReservationTimeResponse;

public record ReservationResponse(long id, MemberNameResponse member,
                                  @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  ThemeResponse theme,
                                  ReservationTimeResponse time) {

    public static ReservationResponse from(Reservation reservation) {
        MemberNameResponse memberResponseDto = new MemberNameResponse(reservation.getMember().getName());
        ReservationTimeResponse timeResponseDto = ReservationTimeResponse.from(reservation.getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservation.getTheme());

        return new ReservationResponse(
                reservation.getId(),
                memberResponseDto,
                reservation.getDate(),
                themeResponse,
                timeResponseDto);
    }
}
