package roomescape.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.reservation.ReservationWaiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(
        Long id,
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        TimeInfo time,
        ThemeInfo theme
) {
    public static WaitingResponse from(ReservationWaiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getUserNameValue(),
                waiting.getWaitingDate(),
                new TimeInfo(waiting.getWaitingTime().getId(), waiting.getWaitingTime().getStartAt()),
                new ThemeInfo(
                        waiting.getWaitingTheme().getId(),
                        waiting.getWaitingTheme().getThemeName(),
                        waiting.getWaitingTheme().getThumbnailUrl(),
                        waiting.getWaitingTheme().getDescription())
        );
    }

    private record TimeInfo(
            Long id,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
            LocalTime startAt) {
    }

    private record ThemeInfo(Long id,
                             String name,
                             String thumbnailUrl,
                             String description
    ) {
    }
}
