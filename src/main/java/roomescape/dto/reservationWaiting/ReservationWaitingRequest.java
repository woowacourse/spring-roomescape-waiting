package roomescape.dto.reservationWaiting;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidInputException;

public record ReservationWaitingRequest (String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationWaitingRequest {
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

    public ReservationWaiting toReservationWaiting(ReservationTime reservationTime, Theme theme) {
        return new ReservationWaiting(name, new ReservationSlot(date, reservationTime, theme));
    }
}
