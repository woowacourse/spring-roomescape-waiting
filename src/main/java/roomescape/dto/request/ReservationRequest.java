package roomescape.dto.request;

import java.time.LocalDate;

public interface ReservationRequest {
    LocalDate date();
    Long timeId();
    Long themeId();
}
