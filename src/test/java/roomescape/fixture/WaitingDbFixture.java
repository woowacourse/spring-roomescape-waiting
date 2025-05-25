package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.WaitingRepository;

import java.time.LocalDate;

@Component
public class WaitingDbFixture {

    private final WaitingRepository waitingRepository;

    public WaitingDbFixture(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting 예약_대기_한스_25_4_22_10시_공포(Member member, ReservationTime reservationTime, Theme theme) {
        LocalDate date = ReservationDateFixture.예약날짜_25_4_22.getDate();

        return waitingRepository.save(Waiting.from(date, member, theme, reservationTime));
    }

    public Waiting 예약_대기_생성_한스(Member member, ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        LocalDate date = reservationDate.getDate();

        return waitingRepository.save(Waiting.from(date, member, theme, reservationTime));
    }
}
