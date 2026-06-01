package roomescape.service;

import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationWaitDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWait;
import roomescape.dto.WaitingResponseResult;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservationwait.ReservationWaitAlreadyExistsException;

@Service
@Transactional(readOnly = true)
public class ReservationWaitService {

    private final ReservationWaitDao reservationWaitDao;
    private final ReservationDao reservationDao;

    public ReservationWaitService(ReservationWaitDao reservationWaitDao, ReservationDao reservationDao) {
        this.reservationWaitDao = reservationWaitDao;
        this.reservationDao = reservationDao;
    }

    public List<WaitingResponseResult> getWaitings(Long memberId) {
        return reservationWaitDao.findWaitingsByMemberId(memberId).stream()
                .map(WaitingResponseResult::from)
                .toList();
    }

    @Transactional
    public ReservationWait createReservationWait(Long memberId, Long reservationId) {
        Reservation reservation = findReservation(reservationId);
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
            return reservationDao.findReservationById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationNotFoundException();
        }
    }
}
