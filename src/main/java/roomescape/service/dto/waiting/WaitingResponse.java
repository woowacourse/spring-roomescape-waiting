package roomescape.service.dto.waiting;

import roomescape.domain.reservation.Waiting;

public record WaitingResponse(long id, String name, String theme, String date, String startAt) {

    public WaitingResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getReservation().getTheme().getName(),
                waiting.getReservation().getDate().toString(),
                waiting.getReservation().getTime().getStartAt().toString()
        );
    }
}
