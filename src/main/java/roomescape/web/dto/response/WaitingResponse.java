package roomescape.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Waiting;

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
    public static WaitingResponse of(Waiting waiting, int waitingPosition) {
        return new WaitingResponse(
                waiting.id(),
                waiting.name(),
                waiting.reservationDate(),
                new TimeInfo(waiting.reservationTime().id(), waiting.reservationTime().startAt()),
                new ThemeInfo(
                        waiting.reservationTheme().id(),
                        waiting.reservationTheme().name(),
                        waiting.reservationTheme().thumbnailUrl(),
                        waiting.reservationTheme().description())
        );
    }

    public static WaitingResponse from(Waiting waiting) {
        return of(waiting, 0);
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
