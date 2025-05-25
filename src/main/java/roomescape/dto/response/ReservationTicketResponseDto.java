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

    public ReservationTicketResponseDto(ReservationTicket reservationTicket) {
        this(
                reservationTicket.getId(),
                new MemberResponseDto(reservationTicket.getMember()),
                reservationTicket.getDate(),
                new ReservationTimeResponseDto(reservationTicket.getReservationTime()),
                new ThemeResponseDto(reservationTicket.getTheme())
        );
    }

}
