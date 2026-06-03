package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailImgUrl,
        Long timeId,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
    public static ReservationResponse from(ReservationDetail detail) {
        return new ReservationResponse(
                detail.reservationId(), detail.username(), detail.date(),
                detail.themeId(), detail.themeName(), detail.themeDescription(), detail.thumbnailImgUrl(),
                detail.timeId(), detail.startAt()
        );
    }

    public static ReservationResponse from(Reservation reservation, Theme theme, ReservationTime time) {
        return new ReservationResponse(
                reservation.getId(), reservation.getName(), reservation.getDate(),
                theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailImgUrl(),
                time.getId(), time.getStartAt()
        );
    }

    public static ReservationResponse from(Waiting waiting, Theme theme, ReservationTime time) {
        return new ReservationResponse(
                waiting.getId(), waiting.getName(), waiting.getDate(),
                theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailImgUrl(),
                time.getId(), time.getStartAt()
        );
    }
}
