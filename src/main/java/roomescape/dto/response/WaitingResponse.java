package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Slot;
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
        Slot slot = waiting.slot();
        return new WaitingResponse(
                waiting.id(),
                waiting.name(),
                slot.date(),
                new TimeInfo(slot.time().id(), slot.time().startAt()),
                new ThemeInfo(
                        slot.theme().id(),
                        slot.theme().name(),
                        slot.theme().thumbnailUrl(),
                        slot.theme().description())
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
