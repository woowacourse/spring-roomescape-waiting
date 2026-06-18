package roomescape.reservation.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
import roomescape.theme.web.dto.ThemeResponseDto;
import roomescape.time.web.dto.TimeResponseDto;

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
