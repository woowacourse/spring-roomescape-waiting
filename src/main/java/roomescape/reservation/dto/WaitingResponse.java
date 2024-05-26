package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.model.Waiting;

public record WaitingResponse(Long id, String memberName, String theme, LocalDate date,
                              @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName().getValue(),
                waiting.getReservation().getTheme().getName().getValue(),
                waiting.getReservation().getDate().getValue(),
                waiting.getReservation().getTime().getStartAt()
        );
    }
}
