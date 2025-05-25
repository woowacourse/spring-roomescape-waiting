package roomescape.reservation.infrastructure;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.ReservationRepository;

@Repository
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public JpaReservationRepositoryAdapter(final JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> findByReservationMemberId(final Long memberId) {
        return jpaReservationRepository.findByReservationMemberId(memberId);
    }

    @Override
    public void deleteByReservationSlotIdAndMemberId(final Long reservationId, final Long memberId) {
        jpaReservationRepository.deleteByReservationSlotIdAndMemberId(reservationId, memberId);
    }

    @Override
    public boolean existsByReservationSlotIdAndMemberId(final Long reservationId, final Long memberId) {
        return jpaReservationRepository.existsByReservationSlotIdAndMemberId(reservationId, memberId);
    }

    @Override
    public List<Reservation> findAllByReservationStatus(final ReservationStatus reservationStatus) {
        return jpaReservationRepository.findAllByReservationStatus(reservationStatus);
    }

    @Override
    public void deleteById(final Long waitingId) {
        jpaReservationRepository.deleteById(waitingId);
    }
}
