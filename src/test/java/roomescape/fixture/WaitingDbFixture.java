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

    public Waiting 첫번째_대기(ReservationInfo reservationInfo, Member waitingMember) {
        return waitingRepository.save(Waiting.create(reservationInfo, waitingMember, 1L));
    }

    public Waiting 두번째_대기(ReservationInfo reservationInfo, Member waitingMember) {
        return waitingRepository.save(Waiting.create(reservationInfo, waitingMember, 2L));
    }
}
