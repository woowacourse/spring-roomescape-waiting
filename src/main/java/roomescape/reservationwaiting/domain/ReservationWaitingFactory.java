package roomescape.reservationwaiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Component
public class ReservationWaitingFactory {

    public ReservationWaiting create(Member member, LocalDate date, ReservationTime time, Theme theme) {
        validate(member, date, time, theme);
        return ReservationWaiting.restore(null, member, date, time, theme);
    }

    private void validate(Member member, LocalDate date, ReservationTime time, Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("예약자는 필수입니다.");
        }
        if (date == null || time == null || theme == null) {
            throw new IllegalArgumentException("날짜, 시간, 테마는 필수입니다.");
        }
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAST_TIME_WAITING);
        }
    }
}