package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.member.MemberNameResponseDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.dto.time.ReservationTimeResponseDto;

import java.time.LocalDate;

public record ReservationResponseDto(long id, MemberNameResponseDto member,
                                     @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                     ThemeResponseDto theme,
                                     ReservationTimeResponseDto time) {

    public static ReservationResponseDto of(Reservation reservation, ReservationTime reservationTime, Theme theme) {
        MemberNameResponseDto memberResponseDto = new MemberNameResponseDto(reservation.getMember().getName());
        ReservationTimeResponseDto timeResponseDto = ReservationTimeResponseDto.from(reservationTime);
        ThemeResponseDto themeResponseDto = ThemeResponseDto.from(theme);

        return new ReservationResponseDto(
                reservation.getId(),
                memberResponseDto,
                reservation.getDate(),
                themeResponseDto,
                timeResponseDto);
    }
}
