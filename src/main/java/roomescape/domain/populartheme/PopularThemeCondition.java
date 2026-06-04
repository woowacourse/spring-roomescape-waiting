package roomescape.domain.populartheme;

import java.time.LocalDate;

public class PopularThemeCondition {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int limit;

    public PopularThemeCondition(LocalDate startDate, LocalDate endDate, int limit) {
        validateDate(startDate);
        validateDate(endDate);
        validateDateRange(startDate, endDate);
        validateLimit(limit);

        this.startDate = startDate;
        this.endDate = endDate;
        this.limit = limit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getLimit() {
        return limit;
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 비어 있을 수 없습니다.");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 늦을 수 없습니다.");
        }
    }

    private void validateLimit(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit은 양수이어야 합니다.");
        }
    }
}
