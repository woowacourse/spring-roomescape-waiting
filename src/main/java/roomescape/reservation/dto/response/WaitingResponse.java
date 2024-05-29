package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(Long id, String name, String theme, LocalDate date, LocalTime startAt) {

    public static WaitingResponse from(Waiting waiting) {
        Long id = waiting.getId();
        String name = waiting.getMember().getName();
        String theme = waiting.getTheme().getName();
        LocalDate date = waiting.getDate();
        LocalTime startAt = waiting.getTime().getStartAt();

        return new WaitingResponse(id, name, theme, date, startAt);
    }
}
