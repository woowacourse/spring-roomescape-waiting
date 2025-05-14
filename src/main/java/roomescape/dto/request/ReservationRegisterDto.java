package roomescape.dto.request;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public record ReservationRegisterDto(
        String date,
        Long timeId,
        Long themeId
) {
    public ReservationRegisterDto {

        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("예약 날짜는 null이거나 공백일 수 없습니다");
        }

        if (timeId == null) {
            throw new IllegalArgumentException("예약 시각은 null 일 수 없습니다.");
        }

        if (themeId == null) {
            throw new IllegalArgumentException("테마는 null 일 수 없습니다.");
        }
    }

    public Reservation convertToReservation(ReservationTime reservationTime, Theme theme, Member member) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            return new Reservation(parsedDate, reservationTime, theme, member, LocalDate.now());
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("날짜 형식이 잘못되었습니다", e);
        }
    }
}
