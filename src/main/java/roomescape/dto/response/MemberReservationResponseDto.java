package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.model.ReservationTicket;

public record MemberReservationResponseDto(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time
) {
    public MemberReservationResponseDto(ReservationTicket reservationTicket) {
        this(
                reservationTicket.getId(),
                reservationTicket.getTheme().getName(),
                reservationTicket.getDate(),
                reservationTicket.getReservationTime().getStartAt()
        );
    }
}
