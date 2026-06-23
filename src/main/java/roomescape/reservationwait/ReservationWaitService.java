package roomescape.reservationwait;

import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.Reservation;
import roomescape.reservationwait.dto.WaitingResult;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationwait.exception.ReservationWaitAlreadyExistsException;
import roomescape.reservationwait.exception.PendingReservationWaitNotAllowedException;

@Service
@Transactional(readOnly = true)
public class ReservationWaitService {

    private final ReservationWaitDao reservationWaitDao;
    private final ReservationDao reservationDao;

    public ReservationWaitService(ReservationWaitDao reservationWaitDao, ReservationDao reservationDao) {
        this.reservationWaitDao = reservationWaitDao;
        this.reservationDao = reservationDao;
    }

    public List<WaitingResult> getWaitings(Long memberId) {
        return reservationWaitDao.findWaitingsByMemberId(memberId).stream()
                .map(WaitingResult::from)
                .toList();
    }

    @Transactional
    public ReservationWait createReservationWait(Long memberId, Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (!reservation.isConfirmed()) {
            throw new PendingReservationWaitNotAllowedException();
        }
        ReservationWait candidate = ReservationWait.create(reservation, memberId);
        try {
            return reservationWaitDao.insert(candidate);
        } catch (DuplicateKeyException e) {
            throw new ReservationWaitAlreadyExistsException();
        }
    }

    @Transactional
    public void deleteReservationWait(Long reservationId, Long memberId) {
        reservationWaitDao.deleteByReservationIdAndMemberId(reservationId, memberId);
    }

    private Reservation findReservation(Long id) {
        try {
            return reservationDao.findReservationByIdForUpdate(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationNotFoundException();
        }
    }
}
