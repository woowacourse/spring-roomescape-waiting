package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.model.ReservationTicket;

public record ReservationTicketResponseDto(
        Long id,
        MemberResponseDto member,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {

    public ReservationTicketResponseDto(ReservationTicket reservationTicketInfo) {
        this(
                reservationTicketInfo.getId(),
                new MemberResponseDto(reservationTicketInfo.getMember()),
                reservationTicketInfo.getDate(),
                new ReservationTimeResponseDto(reservationTicketInfo.getReservationTime()),
                new ThemeResponseDto(reservationTicketInfo.getTheme())
        );
    }
}
