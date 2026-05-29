package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;

import java.time.LocalDate;

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
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme())
        );
    }
}
