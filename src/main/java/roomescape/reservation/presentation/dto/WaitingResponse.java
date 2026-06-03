package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;

public record WaitingResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailImgUrl,
        Long timeId,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        Long order
) {
    public static WaitingResponse from(WaitingOrderDetail detail) {
        return new WaitingResponse(
                detail.waitingId(), detail.username(), detail.date(),
                detail.themeId(), detail.themeName(), detail.themeDescription(), detail.thumbnailImgUrl(),
                detail.timeId(), detail.startAt(), detail.order()
        );
    }
}
