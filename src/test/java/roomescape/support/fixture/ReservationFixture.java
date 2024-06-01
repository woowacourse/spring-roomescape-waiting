package roomescape.support.fixture;

import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;

import java.util.List;

@Component
public class ReservationFixture {

    private final MemberReservationRepository reservationRepository;

    public ReservationFixture(MemberReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public MemberReservation createReservation(final ReservationDetail reservationDetail, final Member member) {
        MemberReservation reservation = new MemberReservation(reservationDetail, member, ReservationStatus.RESERVED);
        return reservationRepository.save(reservation);
    }

    public MemberReservation createWaiting(final ReservationDetail reservationDetail, final Member member) {
        MemberReservation waitingReservation = new MemberReservation(reservationDetail, member, ReservationStatus.WAITING);
        return reservationRepository.save(waitingReservation);
    }

    public MemberReservation findById(final Long id) {
        return reservationRepository.findById(id).get();
    }

    public List<MemberReservation> findAll() {
        return reservationRepository.findAll();
    }
}
