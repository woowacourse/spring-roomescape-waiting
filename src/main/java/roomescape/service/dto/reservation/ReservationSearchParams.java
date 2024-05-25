package roomescape.service.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationSearchParams {

    private final ReservationStatus status;
    private final String email;
    private final Long themeId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ReservationSearchParams(String status,
                                   String email,
                                   Long themeId,
                                   LocalDate startDate,
                                   LocalDate endDate) {
        this.status = ReservationStatus.fromString(status);
        this.email = email;
        this.themeId = themeId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getEmail() {
        return email;
    }

    public Long getThemeId() {
        return themeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
