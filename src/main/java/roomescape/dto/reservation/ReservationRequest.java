package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidInputException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record ReservationRequest(String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        List<String> emptyFields = new ArrayList<>();

        if (name == null || name.isBlank()) {
            emptyFields.add("name");
        }

        if (date == null) {
            emptyFields.add("date");
        }

        if (timeId == null) {
            emptyFields.add("timeId");
        }

        if (themeId == null) {
            emptyFields.add("themeId");
        }

        if (!emptyFields.isEmpty()) {
            throw new InvalidInputException("%s 필드가 비어있습니다.".formatted(emptyFields));
        }
    }

    public Reservation to(ReservationTime reservationTime, Theme theme) {
        return Reservation.create(name, date, reservationTime, theme);
    }
}
