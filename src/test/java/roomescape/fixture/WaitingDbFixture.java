package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.ReservationInfo;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.WaitingRepository;

@Component
public class WaitingDbFixture {

    private final WaitingRepository waitingRepository;

    public WaitingDbFixture(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting 대기_25_4_23_10시_공포(ReservationInfo reservationInfo, Member waitingMember) {
        return waitingRepository.save(Waiting.create(reservationInfo, waitingMember, 1L));
    }
}
