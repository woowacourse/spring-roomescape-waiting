package roomescape.integration.fixture;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationSchedule;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.WaitingRepository;

@Component
public class WaitingDbFixture {

    private final WaitingRepository waitingRepository;

    public WaitingDbFixture(final WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting 대기_25_4_22(final ReservationTime time, final Theme theme, final Member member) {
        ReservationDate date = ReservationDateFixture.예약날짜_25_4_22;
        return createWaiting(date, time, theme, member, LocalDateTime.now());
    }

    public Waiting 대기_생성(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member,
            final LocalDateTime waitStartAt
    ) {
        return createWaiting(date, time, theme, member, waitStartAt);
    }

    public Waiting createWaiting(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member,
            final LocalDateTime waitStartAt
    ) {
        return waitingRepository.save(new Waiting(null, member, new ReservationSchedule(date, time, theme), waitStartAt));
    }
} 