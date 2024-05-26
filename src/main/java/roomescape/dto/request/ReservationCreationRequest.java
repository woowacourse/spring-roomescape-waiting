package roomescape.dto.request;

import java.time.LocalDate;

public interface ReservationCreationRequest {

    LocalDate getDate();

    Long getTimeId();

    Long getThemeId();
}
