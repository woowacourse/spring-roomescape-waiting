package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record AdminReservationResponseDto(
        Long id,
        String name,
        LocalDate date,
        @JsonProperty("theme") ThemeResponseDto themeResponseDto,
        @JsonProperty("time") TimeResponseDto timeResponseDto,
        ReservationStatus status,
        LocalDateTime deletedAt
) {
    public static AdminReservationResponseDto from(Reservation reservation) {
        return new AdminReservationResponseDto(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                ThemeResponseDto.from(reservation.getTheme()),
                TimeResponseDto.from(reservation.getTime()),
                reservation.getStatus(),
                reservation.getDeletedAt()
        );
    }
}
