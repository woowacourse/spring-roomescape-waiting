package roomescape.dto.waiting;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.member.MemberNameResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.dto.time.ReservationTimeResponseDto;

import java.time.LocalDate;

public record WaitingResponseDto(long id, MemberNameResponseDto member,
                                 @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                 ThemeResponseDto theme,
                                 ReservationTimeResponseDto time) {
    public static WaitingResponseDto of(Waiting waiting, ReservationTime reservationTime, Theme theme) {
        MemberNameResponseDto memberResponseDto = new MemberNameResponseDto(waiting.getMember().getName());
        ReservationTimeResponseDto timeResponseDto = ReservationTimeResponseDto.from(reservationTime);
        ThemeResponseDto themeResponseDto = ThemeResponseDto.from(theme);

        return new WaitingResponseDto(
                waiting.getId(),
                memberResponseDto,
                waiting.getDate(),
                themeResponseDto,
                timeResponseDto);
    }
}
