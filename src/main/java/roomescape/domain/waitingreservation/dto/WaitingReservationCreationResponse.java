package roomescape.domain.waitingreservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.WaitingReservation;

public record WaitingReservationCreationResponse(
    Long id,
    String name,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm")
    LocalTime time,
    ThemePayload theme,
    LocalDateTime createdAt
) {

    public static WaitingReservationCreationResponse from(WaitingReservation waitingReservation) {
        return new WaitingReservationCreationResponse(
            waitingReservation.getId(),
            waitingReservation.getMember().getName(),
            waitingReservation.getDate().getPlayDay(),
            waitingReservation.getTime().getStartAt(),
            ThemePayload.from(waitingReservation.getTheme()),
            waitingReservation.getCreatedAt()
        );
    }

    public record ThemePayload(String name, String content, String url) {

        private static ThemePayload from(Theme theme) {
            return new ThemePayload(theme.getName(), theme.getContent(), theme.getUrl());
        }
    }
}
