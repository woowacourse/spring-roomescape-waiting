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
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.id(),
                waiting.name(),
                waiting.waitingDate(),
                new TimeInfo(waiting.waitingTime().id(), waiting.waitingTime().startAt()),
                new ThemeInfo(
                        waiting.waitingTheme().id(),
                        waiting.waitingTheme().name(),
                        waiting.waitingTheme().thumbnailUrl(),
                        waiting.waitingTheme().description())
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
