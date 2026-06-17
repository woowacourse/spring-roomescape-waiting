package roomescape.reservation.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.theme.web.ThemeResponseDto;
import roomescape.time.web.TimeResponseDto;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;

public record ReservationResponseDto(
        Long id,
        String name,
        LocalDate date,
        ReservationStatus status,
        @JsonProperty("theme") ThemeResponseDto themeResponseDto,
        @JsonProperty("time") TimeResponseDto timeResponseDto
) {
    public static ReservationResponseDto from(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                reservation.getStatus(),
                ThemeResponseDto.from(reservation.getTheme()),
                TimeResponseDto.from(reservation.getTime())
        );
    }
}
