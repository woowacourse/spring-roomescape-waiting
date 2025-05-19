package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.member.MemberNameResponseDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.dto.time.ReservationTimeResponseDto;

public record ReservationResponseDto(long id, MemberNameResponseDto member,
                                     @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                     ThemeResponseDto theme,
                                     ReservationTimeResponseDto time) {

    public static ReservationResponseDto from(Reservation reservation) {
        MemberNameResponseDto memberResponseDto = new MemberNameResponseDto(reservation.getMember().getName());
        ReservationTimeResponseDto timeResponseDto = ReservationTimeResponseDto.from(reservation.getTime());
        ThemeResponseDto themeResponseDto = ThemeResponseDto.from(reservation.getTheme());

        return new ReservationResponseDto(
                reservation.getId(),
                memberResponseDto,
                reservation.getDate(),
                themeResponseDto,
                timeResponseDto);
    }
}
