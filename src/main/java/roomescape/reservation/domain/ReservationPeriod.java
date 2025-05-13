package roomescape.reservation.domain;

import java.time.LocalDate;

public class ReservationPeriod {

    private final LocalDate standardDate;
    private final int startOffset;
    private final int endOffset;

    public ReservationPeriod(LocalDate standardDate, int startOffset, int endOffset) {
        validateOffset(startOffset, endOffset);
        this.standardDate = standardDate;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public LocalDate findStartDate() {
        return standardDate.minusDays(startOffset);
    }

    public LocalDate findEndDate() {
        return standardDate.minusDays(endOffset);
    }

    private void validateOffset(int startOffset, int endOffset) {
        if (startOffset < 0 || endOffset < 0) {
            throw new IllegalArgumentException("Offset은 0 이상이어야 합니다.");
        }
        if (endOffset > startOffset) {
            throw new IllegalArgumentException("endOffset는 startOffset보다 클 수 없습니다. (종료일은 시작일보다 미래일 수 없습니다)");
        }
    }
}
