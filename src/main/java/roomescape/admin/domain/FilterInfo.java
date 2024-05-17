package roomescape.admin.domain;

import java.time.LocalDate;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.domain.Date;
import roomescape.reservation.exception.ReservationExceptionCode;

public class FilterInfo {

    private final Long memberId;
    private final Long themeId;
    private final Date fromDate;
    private final Date toDate;

    public FilterInfo(Long memberId, Long themeId, LocalDate fromDate, LocalDate toDate) {
        validation(memberId, themeId, fromDate, toDate);
        this.memberId = memberId;
        this.themeId = themeId;
        this.fromDate = Date.dateFrom(fromDate);
        this.toDate = Date.dateFrom(toDate);
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    private void validation(Long memberId, Long themeId, LocalDate fromDate, LocalDate toDate) {
        if (memberId == null) {
            throw new RoomEscapeException(ReservationExceptionCode.MEMBER_INFO_IS_NULL_EXCEPTION);
        }
        if (themeId == null) {
            throw new RoomEscapeException(ReservationExceptionCode.THEME_INFO_IS_NULL_EXCEPTION);
        }
        if (fromDate == null) {
            throw new RoomEscapeException(ReservationExceptionCode.DATE_IS_NULL_EXCEPTION);
        }
        if (toDate == null) {
            throw new RoomEscapeException(ReservationExceptionCode.DATE_IS_NULL_EXCEPTION);
        }
    }
}
