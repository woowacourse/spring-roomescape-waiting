package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public record ReservationTicketRegisterDto(
        @NotBlank String date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {

    public ReservationTicket convertToReservation(ReservationTime reservationTime, Theme theme, Member member) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            return new ReservationTicket(new Reservation(parsedDate, reservationTime, theme, member, LocalDate.now()));
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("날짜 형식이 잘못되었습니다", e);
        }
    }
}
