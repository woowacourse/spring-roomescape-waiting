package roomescape.integration.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.repository.ReservationRepository;

@Component
public class ReservationDbFixture {
    private ReservationRepository reservationRepository;

    public ReservationDbFixture(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation 예약_생성(
            final ReservationSchedule schedule,
            final Member member
    ) {
        return reservationRepository.save(new Reservation(null, member, schedule));
    }
}
