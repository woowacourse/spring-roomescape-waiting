package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.jpa.JpaReservationWaitingDao;

@Repository
public class JpaReservationWaitingRepository implements ReservationWaitingRepository {
    private final JpaReservationWaitingDao waitingDao;

    public JpaReservationWaitingRepository(JpaReservationWaitingDao waitingDao) {
        this.waitingDao = waitingDao;
    }

    @Override
    public ReservationWaiting save(ReservationWaiting reservationWaiting) {
        return waitingDao.save(reservationWaiting);
    }

    @Override
    public List<ReservationWaiting> findAll() {
        return waitingDao.findAll();
    }

    @Override
    public List<ReservationWaiting> findAllByMemberId(long memberId) {
        return waitingDao.findAllByWaitingMember_Id(memberId);
    }

    @Override
    public List<ReservationWaiting> findByReservation(Reservation reservation) {
        return waitingDao.findAllByReservation(reservation);
    }

    @Override
    public Optional<ReservationWaiting> findTopWaitingByReservation(Reservation reservation) {
        return waitingDao.findTopByReservationOrderByCreateAt(reservation);
    }

    @Override
    public boolean existsByReservationAndWaitingMember(Reservation reservation,
                                                       Member waitingMember) {
        return waitingDao.existsByReservationAndWaitingMember(reservation, waitingMember);
    }

    @Override
    public void delete(long id) {
        waitingDao.deleteById(id);
    }
}
