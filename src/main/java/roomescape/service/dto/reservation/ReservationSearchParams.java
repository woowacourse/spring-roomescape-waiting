package roomescape.service.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationSearchParams {
    private final String email;
    private final Long themeId;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final ReservationStatus status;

    public ReservationSearchParams(String email,
                                   Long themeId,
                                   LocalDate dateFrom,
                                   LocalDate dateTo,
                                   ReservationStatus status) {
        this.email = email;
        this.themeId = themeId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public Long getThemeId() {
        return themeId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
