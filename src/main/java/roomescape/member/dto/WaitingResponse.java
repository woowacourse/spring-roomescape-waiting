package roomescape.member.dto;

import roomescape.reservation.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(Long id, String name, String theme, LocalDate date, LocalTime startAt) {

    public WaitingResponse(Waiting waiting) {
        this(waiting.getId(),
                waiting.getMember().getName().name(),
                waiting.getTheme().getName().name(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt());
    }
}
