package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

public record WaitingResponse(
        Long id,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        WaitingTheme theme,
        WaitingTimeSlot time,
        Long order
) {
    public static WaitingResponse from(WaitingDetail detail) {
        return new WaitingResponse(
                detail.waitingId(), detail.username(), detail.date(),
                new WaitingTheme(detail.themeId(), detail.themeName()),
                new WaitingTimeSlot(detail.timeId(), detail.startAt()),
                detail.order()
        );
    }

    private record WaitingTheme(Long id, String name) {
    }

    private record WaitingTimeSlot(
            Long id,
            @JsonFormat(pattern = "HH:mm") LocalTime startAt
    ) {
    }
}
