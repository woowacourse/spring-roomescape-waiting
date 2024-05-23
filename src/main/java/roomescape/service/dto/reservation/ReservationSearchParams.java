package roomescape.service.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationSearchParams {
    private final String email;
    private final Long themeId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final ReservationStatus status;

    public ReservationSearchParams(String email,
                                   Long themeId,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   ReservationStatus status) {
        this.email = email;
        this.themeId = themeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
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
