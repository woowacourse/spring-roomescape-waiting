package roomescape.service.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationSearchParams {
    private final Long memberId;
    private final Long themeId;
    private final LocalDate dateFrom;
    private final LocalTime dateTo;

    public ReservationSearchParams(Long memberId, Long themeId, LocalDate dateFrom, LocalTime dateTo) {
        this.memberId = memberId;
        this.themeId = themeId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Long memberId() {
        return memberId;
    }

    public Long themeId() {
        return themeId;
    }

    public LocalDate dateFrom() {
        return dateFrom;
    }

    public LocalTime dateTo() {
        return dateTo;
    }
}
