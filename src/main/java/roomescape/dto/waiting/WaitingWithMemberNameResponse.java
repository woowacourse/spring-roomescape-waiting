package roomescape.dto.waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Waiting;

public record WaitingWithMemberNameResponse(Long id, String name, String theme, LocalDate date, LocalTime time) {

    public static WaitingWithMemberNameResponse from(Waiting waiting) {
        return new WaitingWithMemberNameResponse(waiting.getId(), waiting.getMember().
                getName(), waiting.getTheme().getName(), waiting.getDate(),
                waiting.getTime().getStartAt());
    }

}
