package roomescape.integration.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationWait;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.repository.ReservationWaitRepository;

@Component
public class ReservationWaitDbFixture {
    private ReservationWaitRepository reservationWaitRepository;

    public ReservationWaitDbFixture(final ReservationWaitRepository reservationWaitRepository) {
        this.reservationWaitRepository = reservationWaitRepository;
    }

    public ReservationWait createReservationWait(
            final ReservationSchedule schedule,
            final Member member
    ) {
        return reservationWaitRepository.save(new ReservationWait(null, member, schedule));
    }
}
