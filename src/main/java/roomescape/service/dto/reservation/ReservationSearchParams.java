package roomescape.service.dto.reservation;

import java.time.LocalDate;

public class ReservationSearchParams {
    private final String email;
    private final Long themeId;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;

    public ReservationSearchParams(String email, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        this.email = email;
        this.themeId = themeId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
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
}
