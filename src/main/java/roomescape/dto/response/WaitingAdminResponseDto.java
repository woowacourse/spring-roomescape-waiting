package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.Waiting;

public record WaitingAdminResponseDto(
        Long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime startAt
) {
    public WaitingAdminResponseDto(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getReservationSpec().getMember().getName(),
                waiting.getThemeName(),
                waiting.getReservationDate(),
                waiting.getReservationTime().getStartAt()
        );
    }
}
