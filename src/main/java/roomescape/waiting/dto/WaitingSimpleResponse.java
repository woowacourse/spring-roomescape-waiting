package roomescape.waiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.domain.Waiting;

public record WaitingSimpleResponse(
        long id,
        String name,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
    public WaitingSimpleResponse(final Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getMemberName().getValue(),
                waiting.getThemeName().getValue(),
                waiting.getDate(),
                waiting.getStartAt()
        );
    }
}
